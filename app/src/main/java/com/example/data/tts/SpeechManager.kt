package com.example.data.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private val TAG = "SpeechManager"
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("tts_voice_preferences", Context.MODE_PRIVATE)

    private var tts: TextToSpeech? = TextToSpeech(appContext, this)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentUtteranceId = MutableStateFlow<String?>(null)
    val currentUtteranceId: StateFlow<String?> = _currentUtteranceId.asStateFlow()

    private val _currentWordRange = MutableStateFlow<Pair<Int, Int>?>(null)
    val currentWordRange: StateFlow<Pair<Int, Int>?> = _currentWordRange.asStateFlow()

    private val _speechRate = MutableStateFlow(prefs.getFloat("pref_tts_rate", 1.0f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(prefs.getFloat("pref_tts_pitch", 1.0f))
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _currentLocale = MutableStateFlow(Locale.US)
    val currentLocale: StateFlow<Locale> = _currentLocale.asStateFlow()

    // Voices catalog and active voice
    private val _availableVoices = MutableStateFlow<List<SystemVoiceInfo>>(CURATED_VOICES)
    val availableVoices: StateFlow<List<SystemVoiceInfo>> = _availableVoices.asStateFlow()

    private val _currentVoice = MutableStateFlow<SystemVoiceInfo?>(CURATED_VOICES.first())
    val currentVoice: StateFlow<SystemVoiceInfo?> = _currentVoice.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Listener callback when utterance finishes (used for auto-advance)
    var onLineCompleted: ((lineId: String) -> Unit)? = null

    // Pending speech if speak() is invoked before initialization completes
    private var pendingSpeakText: Pair<String, String>? = null

    init {
        // Init happens via callback
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isInitialized.value = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                    _currentUtteranceId.value = utteranceId
                    _currentWordRange.value = null
                }

                override fun onDone(utteranceId: String?) {
                    _isPlaying.value = false
                    _currentUtteranceId.value = null
                    _currentWordRange.value = null
                    utteranceId?.let { id ->
                        onLineCompleted?.invoke(id)
                    }
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    super.onRangeStart(utteranceId, start, end, frame)
                    _currentWordRange.value = Pair(start, end)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isPlaying.value = false
                    _currentUtteranceId.value = null
                    _currentWordRange.value = null
                    _errorMessage.value = "Speech synthesis error for utterance: $utteranceId"
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isPlaying.value = false
                    _currentUtteranceId.value = null
                    _currentWordRange.value = null
                    _errorMessage.value = "TTS error code: $errorCode"
                    Log.e(TAG, "TTS Error code: $errorCode for $utteranceId")
                }
            })

            // Populate system voices dynamically if available on the engine
            syncVoicesFromEngine()

            // Restore saved voice or fallback to default
            val savedVoiceId = prefs.getString("pref_selected_voice_id", "en-us-natural-f")
            val targetVoice = _availableVoices.value.find { it.id == savedVoiceId } ?: _availableVoices.value.first()
            setVoice(targetVoice)

            // Apply rate and pitch
            tts?.setSpeechRate(_speechRate.value)
            tts?.setPitch(_pitch.value)

            // Execute pending speak if any
            pendingSpeakText?.let { (text, id) ->
                pendingSpeakText = null
                speak(text, id)
            }
            Log.d(TAG, "TextToSpeech successfully initialized with voice: ${targetVoice.displayName}")
        } else {
            _isInitialized.value = false
            _errorMessage.value = "TTS Initialization failed (Status code: $status)"
            Log.e(TAG, "Failed to initialize TextToSpeech")
        }
    }

    private fun syncVoicesFromEngine() {
        try {
            val engineVoices: Set<Voice>? = tts?.voices
            if (!engineVoices.isNullOrEmpty()) {
                val mappedVoices = mutableListOf<SystemVoiceInfo>()
                // First include our polished curated list
                mappedVoices.addAll(CURATED_VOICES)

                // Augment with any unique engine voices discovered
                for (v in engineVoices) {
                    if (v.isNetworkConnectionRequired) continue // Prefer offline/local
                    val id = v.name
                    if (mappedVoices.none { it.id.equals(id, ignoreCase = true) }) {
                        val isFemale = id.contains("female", ignoreCase = true) || id.contains("fem", ignoreCase = true)
                        val isMale = id.contains("male", ignoreCase = true)
                        val gender = when {
                            isFemale -> "Female"
                            isMale -> "Male"
                            else -> "Neutral"
                        }
                        val accent = when (v.locale.country.uppercase()) {
                            "US" -> "American"
                            "GB" -> "British"
                            "IN" -> "Indian"
                            "AU" -> "Australian"
                            "CA" -> "Canadian"
                            else -> v.locale.displayCountry.ifBlank { "Standard" }
                        }
                        mappedVoices.add(
                            SystemVoiceInfo(
                                id = id,
                                name = v.name,
                                displayName = "${v.locale.displayLanguage} (${accent}) - Voice ${v.name.takeLast(4)}",
                                languageCode = v.locale.language,
                                languageName = v.locale.displayLanguage,
                                flag = getFlagForCountry(v.locale.country),
                                locale = v.locale,
                                gender = gender,
                                accent = accent,
                                quality = if (v.quality >= Voice.QUALITY_HIGH) "High Quality" else "Standard"
                            )
                        )
                    }
                }
                _availableVoices.value = mappedVoices
            }
        } catch (e: Exception) {
            Log.d(TAG, "Voices query not supported or empty: ${e.message}")
        }
    }

    fun speak(text: String, utteranceId: String): Boolean {
        val cleanText = text.trim()
        if (cleanText.isEmpty()) return false

        if (!_isInitialized.value || tts == null) {
            pendingSpeakText = Pair(cleanText, utteranceId)
            _isPlaying.value = true
            _currentUtteranceId.value = utteranceId
            return true
        }

        stop()
        _isPlaying.value = true
        _currentUtteranceId.value = utteranceId
        val result = tts?.speak(
            cleanText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
        return result == TextToSpeech.SUCCESS
    }

    fun stop() {
        pendingSpeakText = null
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
        _isPlaying.value = false
        _currentUtteranceId.value = null
        _currentWordRange.value = null
    }

    fun setSpeed(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 2.5f)
        _speechRate.value = clampedRate
        tts?.setSpeechRate(clampedRate)
        prefs.edit().putFloat("pref_tts_rate", clampedRate).apply()
    }

    fun setPitch(pitch: Float) {
        val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
        _pitch.value = clampedPitch
        tts?.setPitch(clampedPitch)
        prefs.edit().putFloat("pref_tts_pitch", clampedPitch).apply()
    }

    fun setVoice(voice: SystemVoiceInfo) {
        _currentVoice.value = voice
        _currentLocale.value = voice.locale
        prefs.edit().putString("pref_selected_voice_id", voice.id).apply()

        // Apply locale
        tts?.setLanguage(voice.locale)

        // If engine supports setting voice by object, try finding it
        try {
            val engineVoices = tts?.voices
            val matchedVoice = engineVoices?.find { it.name.equals(voice.id, ignoreCase = true) }
            if (matchedVoice != null) {
                tts?.setVoice(matchedVoice)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Setting direct voice object failed: ${e.message}")
        }
    }

    fun previewVoice(voice: SystemVoiceInfo, customText: String? = null) {
        stop()
        setVoice(voice)
        val textToSpeak = customText ?: voice.samplePhrase
        speak(textToSpeak, "preview_${voice.id}")
    }

    fun setLanguageByCode(languageCode: String): Boolean {
        val locale = getLocaleForCode(languageCode)
        _currentLocale.value = locale

        // Automatically select the best matching voice for this language
        val matchingVoice = _availableVoices.value.find { it.languageCode.equals(languageCode, ignoreCase = true) }
        if (matchingVoice != null) {
            setVoice(matchingVoice)
        } else {
            tts?.setLanguage(locale)
        }
        return true
    }

    fun getVoicesForLanguage(languageCode: String): List<SystemVoiceInfo> {
        val matching = _availableVoices.value.filter { it.languageCode.equals(languageCode, ignoreCase = true) }
        return if (matching.isNotEmpty()) matching else _availableVoices.value
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
        tts = null
    }

    companion object {
        fun getLocaleForCode(code: String): Locale {
            return when (code.lowercase()) {
                "en" -> Locale.US
                "bn" -> Locale("bn", "IN")
                "hi" -> Locale("hi", "IN")
                "ta" -> Locale("ta", "IN")
                "zh" -> Locale.SIMPLIFIED_CHINESE
                "es" -> Locale("es", "ES")
                "fr" -> Locale.FRENCH
                "de" -> Locale.GERMAN
                "ja" -> Locale.JAPANESE
                "it" -> Locale.ITALIAN
                "pt" -> Locale("pt", "BR")
                "ko" -> Locale.KOREAN
                "ru" -> Locale("ru", "RU")
                "nl" -> Locale("nl", "NL")
                "pl" -> Locale("pl", "PL")
                "ar" -> Locale("ar")
                "tr" -> Locale("tr", "TR")
                else -> Locale(code)
            }
        }

        private fun getFlagForCountry(countryCode: String): String {
            return when (countryCode.uppercase()) {
                "US" -> "🇺🇸"
                "GB", "UK" -> "🇬🇧"
                "IN" -> "🇮🇳"
                "AU" -> "🇦🇺"
                "CA" -> "🇨🇦"
                "CN" -> "🇨🇳"
                "ES" -> "🇪🇸"
                "FR" -> "🇫🇷"
                "DE" -> "🇩🇪"
                "JP" -> "🇯🇵"
                "BR" -> "🇧🇷"
                "MX" -> "🇲🇽"
                else -> "🌐"
            }
        }

        data class SupportedLanguage(
            val code: String,
            val name: String,
            val flag: String,
            val nativeName: String
        )

        val SUPPORTED_LANGUAGES = listOf(
            SupportedLanguage("en", "English", "🇺🇸", "English"),
            SupportedLanguage("bn", "Bengali", "🇮🇳", "বাংলা"),
            SupportedLanguage("hi", "Hindi", "🇮🇳", "हिन्दी"),
            SupportedLanguage("ta", "Tamil", "🇮🇳", "தமிழ்"),
            SupportedLanguage("zh", "Chinese", "🇨🇳", "中文"),
            SupportedLanguage("es", "Spanish", "🇪🇸", "Español"),
            SupportedLanguage("fr", "French", "🇫🇷", "Français"),
            SupportedLanguage("de", "German", "🇩🇪", "Deutsch"),
            SupportedLanguage("ja", "Japanese", "🇯🇵", "日本語"),
            SupportedLanguage("it", "Italian", "🇮🇹", "Italiano"),
            SupportedLanguage("pt", "Portuguese", "🇧🇷", "Português"),
            SupportedLanguage("ru", "Russian", "🇷🇺", "Русский")
        )

        val CURATED_VOICES = listOf(
            // English - US
            SystemVoiceInfo(
                id = "en-us-natural-f",
                name = "Aria",
                displayName = "Aria • US Female (Natural)",
                languageCode = "en",
                languageName = "English (US)",
                flag = "🇺🇸",
                locale = Locale.US,
                gender = "Female",
                accent = "American",
                quality = "High Quality",
                samplePhrase = "Hello! I am Aria, a warm American female voice for reading books and news."
            ),
            SystemVoiceInfo(
                id = "en-us-natural-m",
                name = "Guy",
                displayName = "Guy • US Male (Deep & Clear)",
                languageCode = "en",
                languageName = "English (US)",
                flag = "🇺🇸",
                locale = Locale.US,
                gender = "Male",
                accent = "American",
                quality = "High Quality",
                samplePhrase = "Greetings! I am Guy, a clear American male narrator for articles and literature."
            ),

            // English - UK (British)
            SystemVoiceInfo(
                id = "en-gb-natural-f",
                name = "Sonia",
                displayName = "Sonia • British Female (Refined)",
                languageCode = "en",
                languageName = "English (UK)",
                flag = "🇬🇧",
                locale = Locale.UK,
                gender = "Female",
                accent = "British",
                quality = "Neural",
                samplePhrase = "Good day! I am Sonia, reading your material with a natural British cadence."
            ),
            SystemVoiceInfo(
                id = "en-gb-natural-m",
                name = "Oliver",
                displayName = "Oliver • British Male (Storyteller)",
                languageCode = "en",
                languageName = "English (UK)",
                flag = "🇬🇧",
                locale = Locale.UK,
                gender = "Male",
                accent = "British",
                quality = "High Quality",
                samplePhrase = "Hello there. I am Oliver, an expressive British voice for stories and reports."
            ),

            // English - India
            SystemVoiceInfo(
                id = "en-in-natural-f",
                name = "Priya",
                displayName = "Priya • Indian English Female",
                languageCode = "en",
                languageName = "English (India)",
                flag = "🇮🇳",
                locale = Locale("en", "IN"),
                gender = "Female",
                accent = "Indian",
                quality = "High Quality",
                samplePhrase = "Namaste! I am Priya, reading news and articles in Indian English."
            ),
            SystemVoiceInfo(
                id = "en-in-natural-m",
                name = "Rohan",
                displayName = "Rohan • Indian English Male",
                languageCode = "en",
                languageName = "English (India)",
                flag = "🇮🇳",
                locale = Locale("en", "IN"),
                gender = "Male",
                accent = "Indian",
                quality = "High Quality",
                samplePhrase = "Hello! I am Rohan, an articulate Indian English narrator."
            ),

            // English - Australia
            SystemVoiceInfo(
                id = "en-au-natural-f",
                name = "Natasha",
                displayName = "Natasha • Australian Female",
                languageCode = "en",
                languageName = "English (AU)",
                flag = "🇦🇺",
                locale = Locale("en", "AU"),
                gender = "Female",
                accent = "Australian",
                quality = "High Quality",
                samplePhrase = "G'day! I am Natasha, your friendly Australian voice narrator."
            ),

            // Hindi (हिन्दी)
            SystemVoiceInfo(
                id = "hi-in-natural-f",
                name = "Swara",
                displayName = "स्वरा (Swara) • Hindi Female",
                languageCode = "hi",
                languageName = "Hindi (हिन्दी)",
                flag = "🇮🇳",
                locale = Locale("hi", "IN"),
                gender = "Female",
                accent = "Indian",
                quality = "Neural",
                samplePhrase = "नमस्ते! मैं स्वरा हूँ, आपकी मधुर हिन्दी पुस्तक और समाचार वाचक।"
            ),
            SystemVoiceInfo(
                id = "hi-in-natural-m",
                name = "Kabir",
                displayName = "कबीर (Kabir) • Hindi Male",
                languageCode = "hi",
                languageName = "Hindi (हिन्दी)",
                flag = "🇮🇳",
                locale = Locale("hi", "IN"),
                gender = "Male",
                accent = "Indian",
                quality = "High Quality",
                samplePhrase = "नमस्कार! मैं कबीर हूँ, समाचार, लेख और किताबों के लिए स्पष्ट हिन्दी आवाज़।"
            ),

            // Bengali (বাংলা)
            SystemVoiceInfo(
                id = "bn-in-natural-f",
                name = "Ananya",
                displayName = "অনন্যা (Ananya) • Bengali Female",
                languageCode = "bn",
                languageName = "Bengali (বাংলা)",
                flag = "🇮🇳",
                locale = Locale("bn", "IN"),
                gender = "Female",
                accent = "Indian",
                quality = "Neural",
                samplePhrase = "নমস্কার! আমি অনন্যা, বাংলা গল্প, উপন্যাস এবং সংবাদপত্র পাঠিকা।"
            ),
            SystemVoiceInfo(
                id = "bn-in-natural-m",
                name = "Sourav",
                displayName = "সৌরভ (Sourav) • Bengali Male",
                languageCode = "bn",
                languageName = "Bengali (বাংলা)",
                flag = "🇮🇳",
                locale = Locale("bn", "IN"),
                gender = "Male",
                accent = "Indian",
                quality = "High Quality",
                samplePhrase = "নমস্কার! আমি সৌরভ, বাংলা বই ও সংবাদ পাঠের জন্য গম্ভীর ও স্পষ্ট কণ্ঠ।"
            ),

            // Tamil (தமிழ்)
            SystemVoiceInfo(
                id = "ta-in-natural-f",
                name = "Kavitha",
                displayName = "கவிதா (Kavitha) • Tamil Female",
                languageCode = "ta",
                languageName = "Tamil (தமிழ்)",
                flag = "🇮🇳",
                locale = Locale("ta", "IN"),
                gender = "Female",
                accent = "Indian",
                quality = "High Quality",
                samplePhrase = "வணக்கம்! நான் கவிதா, உங்கள் இனிய தமிழ் வாசிப்பாளர்."
            ),
            SystemVoiceInfo(
                id = "ta-in-natural-m",
                name = "Vijay",
                displayName = "விஜய் (Vijay) • Tamil Male",
                languageCode = "ta",
                languageName = "Tamil (தமிழ்)",
                flag = "🇮🇳",
                locale = Locale("ta", "IN"),
                gender = "Male",
                accent = "Indian",
                quality = "High Quality",
                samplePhrase = "வணக்கம்! நான் விஜய், செய்திகள் மற்றும் புத்தகங்களை தெளிவாக வாசிக்கிறேன்."
            ),

            // Chinese (中文)
            SystemVoiceInfo(
                id = "zh-cn-natural-f",
                name = "Xiaoyan",
                displayName = "小燕 (Xiaoyan) • Mandarin Female",
                languageCode = "zh",
                languageName = "Chinese (中文)",
                flag = "🇨🇳",
                locale = Locale.SIMPLIFIED_CHINESE,
                gender = "Female",
                accent = "Mandarin",
                quality = "High Quality",
                samplePhrase = "您好！我是小燕，为您朗读中文书籍与新闻。"
            ),
            SystemVoiceInfo(
                id = "zh-cn-natural-m",
                name = "Yunxi",
                displayName = "云希 (Yunxi) • Mandarin Male",
                languageCode = "zh",
                languageName = "Chinese (中文)",
                flag = "🇨🇳",
                locale = Locale.SIMPLIFIED_CHINESE,
                gender = "Male",
                accent = "Mandarin",
                quality = "High Quality",
                samplePhrase = "您好！我是云希，声音沉稳清晰，伴您深入阅读。"
            ),

            // Spanish (Español)
            SystemVoiceInfo(
                id = "es-es-natural-f",
                name = "Lucia",
                displayName = "Lucía • Spanish Female (Spain)",
                languageCode = "es",
                languageName = "Spanish (Español)",
                flag = "🇪🇸",
                locale = Locale("es", "ES"),
                gender = "Female",
                accent = "Castilian",
                quality = "High Quality",
                samplePhrase = "¡Hola! Soy Lucía, tu narradora de libros y artículos en español."
            ),
            SystemVoiceInfo(
                id = "es-mx-natural-m",
                name = "Jorge",
                displayName = "Jorge • Spanish Male (LatAm)",
                languageCode = "es",
                languageName = "Spanish (LatAm)",
                flag = "🇲🇽",
                locale = Locale("es", "MX"),
                gender = "Male",
                accent = "Latin American",
                quality = "High Quality",
                samplePhrase = "¡Hola! Soy Jorge, voz clara y fluida para tus lecturas en español."
            ),

            // French (Français)
            SystemVoiceInfo(
                id = "fr-fr-natural-f",
                name = "Camille",
                displayName = "Camille • French Female",
                languageCode = "fr",
                languageName = "French (Français)",
                flag = "🇫🇷",
                locale = Locale.FRANCE,
                gender = "Female",
                accent = "French",
                quality = "High Quality",
                samplePhrase = "Bonjour ! Je suis Camille, votre voix de lecture fluide et naturelle."
            ),

            // German (Deutsch)
            SystemVoiceInfo(
                id = "de-de-natural-m",
                name = "Lukas",
                displayName = "Lukas • German Male",
                languageCode = "de",
                languageName = "German (Deutsch)",
                flag = "🇩🇪",
                locale = Locale.GERMANY,
                gender = "Male",
                accent = "German",
                quality = "High Quality",
                samplePhrase = "Hallo! Ich bin Lukas, Ihre deutsche Vorlesestimme für Bücher und Nachrichten."
            )
        )
    }
}
