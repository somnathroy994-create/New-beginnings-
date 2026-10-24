package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.data.ai.ExtractedScanResult
import com.example.data.ai.GeminiSummaryService
import com.example.data.ai.GeminiVisionService
import com.example.data.ai.GeminiVocabularyService
import com.example.data.ai.TextSummarizer
import com.example.data.local.AppDatabase
import com.example.data.model.Article
import com.example.data.model.Note
import com.example.data.model.SummaryConfig
import com.example.data.model.SummaryFocus
import com.example.data.model.SummaryLength
import com.example.data.model.SummaryResult
import com.example.data.model.SummaryTone
import com.example.data.model.VocabularyWord
import com.example.data.translation.TranslationResult
import com.example.data.translation.TranslatorEngine
import com.example.data.tts.SpeechManager
import com.example.data.tts.SystemVoiceInfo
import com.example.ui.screens.VisualTopic
import com.example.ui.theme.LightBackgroundOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ScanAction {
    READ_ALOUD,
    TRANSLATE,
    SUMMARISE,
    OPEN
}

enum class AppTab {
    LIBRARY,
    READER,
    TRANSLATE,
    VISUALS,
    HABIT_NOTES
}

class ReadAloudViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val articleDao = database.articleDao()
    private val noteDao = database.noteDao()
    private val vocabularyDao = database.vocabularyDao()

    val speechManager = SpeechManager(application)

    // Current navigation tab
    private val _currentTab = MutableStateFlow(AppTab.LIBRARY)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Articles from database
    val articles: StateFlow<List<Article>> = articleDao.getAllArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently opened article in Reader
    private val _selectedArticle = MutableStateFlow<Article?>(null)
    val selectedArticle: StateFlow<Article?> = _selectedArticle.asStateFlow()

    // Current active line index
    private val _currentLineIndex = MutableStateFlow(0)
    val currentLineIndex: StateFlow<Int> = _currentLineIndex.asStateFlow()

    // Continuous auto-advance line-by-line reading
    private val _autoAdvance = MutableStateFlow(true)
    val autoAdvance: StateFlow<Boolean> = _autoAdvance.asStateFlow()

    // Auto-scroll along with reading
    private val _autoScroll = MutableStateFlow(true)
    val autoScroll: StateFlow<Boolean> = _autoScroll.asStateFlow()

    // Translation State
    private val _translationResult = MutableStateFlow<TranslationResult?>(null)
    val translationResult: StateFlow<TranslationResult?> = _translationResult.asStateFlow()

    private val _showDualLanguage = MutableStateFlow(true)
    val showDualLanguage: StateFlow<Boolean> = _showDualLanguage.asStateFlow()

    private val _readTranslatedVoice = MutableStateFlow(false)
    val readTranslatedVoice: StateFlow<Boolean> = _readTranslatedVoice.asStateFlow()

    // Visualizations state
    private val _activeVisualTopic = MutableStateFlow(VisualTopic.HISTORY)
    val activeVisualTopic: StateFlow<VisualTopic> = _activeVisualTopic.asStateFlow()

    // Notes
    val allNotes: StateFlow<List<Note>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vocabulary
    val allVocabulary: StateFlow<List<VocabularyWord>> = vocabularyDao.getAllWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Summary of current article & Gemini configuration
    private val _summaryResult = MutableStateFlow<SummaryResult?>(null)
    val summaryResult: StateFlow<SummaryResult?> = _summaryResult.asStateFlow()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    private val _summaryConfig = MutableStateFlow(SummaryConfig())
    val summaryConfig: StateFlow<SummaryConfig> = _summaryConfig.asStateFlow()

    private val _summaryError = MutableStateFlow<String?>(null)
    val summaryError: StateFlow<String?> = _summaryError.asStateFlow()

    private val _isReadingSummary = MutableStateFlow(false)
    val isReadingSummary: StateFlow<Boolean> = _isReadingSummary.asStateFlow()

    // Dialog & Inspection states
    private val _inspectedWord = MutableStateFlow<VocabularyWord?>(null)
    val inspectedWord: StateFlow<VocabularyWord?> = _inspectedWord.asStateFlow()

    private val _showWordLookupDialog = MutableStateFlow(false)
    val showWordLookupDialog: StateFlow<Boolean> = _showWordLookupDialog.asStateFlow()

    private val _showAddArticleDialog = MutableStateFlow(false)
    val showAddArticleDialog: StateFlow<Boolean> = _showAddArticleDialog.asStateFlow()

    private val _showAddNoteDialog = MutableStateFlow(false)
    val showAddNoteDialog: StateFlow<Boolean> = _showAddNoteDialog.asStateFlow()

    private val _noteTargetLine = MutableStateFlow<Pair<Int, String>?>(null)
    val noteTargetLine: StateFlow<Pair<Int, String>?> = _noteTargetLine.asStateFlow()

    private val _showSummarySheet = MutableStateFlow(false)
    val showSummarySheet: StateFlow<Boolean> = _showSummarySheet.asStateFlow()

    private val _showFlashcards = MutableStateFlow(false)
    val showFlashcards: StateFlow<Boolean> = _showFlashcards.asStateFlow()

    // Scanner Bottom Sheet State
    private val _showScannerSheet = MutableStateFlow(false)
    val showScannerSheet: StateFlow<Boolean> = _showScannerSheet.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _extractedScanResult = MutableStateFlow<ExtractedScanResult?>(null)
    val extractedScanResult: StateFlow<ExtractedScanResult?> = _extractedScanResult.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    private val _isEnrichingWord = MutableStateFlow(false)
    val isEnrichingWord: StateFlow<Boolean> = _isEnrichingWord.asStateFlow()

    // User-customizable light background reading color
    private val prefs = application.getSharedPreferences("reading_habit_prefs", Context.MODE_PRIVATE)
    private val _lightBackgroundOption = MutableStateFlow(
        LightBackgroundOption.fromId(prefs.getString("bg_light_color", LightBackgroundOption.CRISP_WHITE.id))
    )
    val lightBackgroundOption: StateFlow<LightBackgroundOption> = _lightBackgroundOption.asStateFlow()

    private val _showColorPicker = MutableStateFlow(false)
    val showColorPicker: StateFlow<Boolean> = _showColorPicker.asStateFlow()

    // Voice Selection Modal Dialog
    private val _showVoiceSelectionDialog = MutableStateFlow(false)
    val showVoiceSelectionDialog: StateFlow<Boolean> = _showVoiceSelectionDialog.asStateFlow()

    // Filters
    private val _selectedLanguageFilter = MutableStateFlow("all")
    val selectedLanguageFilter: StateFlow<String> = _selectedLanguageFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("all")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    init {
        // Set speech manager callback for line completion
        speechManager.onLineCompleted = { lineId ->
            if (_autoAdvance.value) {
                val currentArticle = _selectedArticle.value
                if (currentArticle != null) {
                    val lines = currentArticle.getLines()
                    val nextIndex = _currentLineIndex.value + 1
                    if (nextIndex < lines.size) {
                        viewModelScope.launch {
                            _currentLineIndex.value = nextIndex
                            articleDao.updateReadingProgress(currentArticle.id, nextIndex)
                            val textToSpeak = if (_readTranslatedVoice.value && _translationResult.value != null) {
                                _translationResult.value?.lineByLine?.getOrNull(nextIndex)?.second ?: lines[nextIndex]
                            } else {
                                lines[nextIndex]
                            }
                            speechManager.speak(textToSpeak, "line_$nextIndex")
                        }
                    }
                }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openArticle(article: Article, initialLine: Int? = null) {
        _selectedArticle.value = article
        val lineToRead = initialLine ?: article.lastReadLineIndex.coerceIn(0, (article.getLines().size - 1).coerceAtLeast(0))
        _currentLineIndex.value = lineToRead
        speechManager.setLanguageByCode(article.languageCode)

        // Automatically prepare Bengali / Hindi translation preview for reader
        val defaultTarget = if (article.languageCode == "bn") "en" else "bn"
        _translationResult.value = TranslatorEngine.translateArticleContent(
            article,
            defaultTarget,
            "Translate to $defaultTarget"
        )

        _currentTab.value = AppTab.READER
    }

    fun playOrPause() {
        if (speechManager.isPlaying.value) {
            speechManager.stop()
        } else {
            val article = _selectedArticle.value ?: return
            val lines = article.getLines()
            if (lines.isEmpty()) return
            val index = _currentLineIndex.value.coerceIn(0, lines.size - 1)

            if (_readTranslatedVoice.value && _translationResult.value != null) {
                val transResult = _translationResult.value!!
                speechManager.setLanguageByCode(transResult.targetLanguageCode)
                val transLine = transResult.lineByLine.getOrNull(index)?.second ?: lines[index]
                speechManager.speak(transLine, "line_$index")
            } else {
                speechManager.setLanguageByCode(article.languageCode)
                speechManager.speak(lines[index], "line_$index")
            }
        }
    }

    fun readLine(index: Int) {
        val article = _selectedArticle.value ?: return
        val lines = article.getLines()
        if (index in lines.indices) {
            _currentLineIndex.value = index
            viewModelScope.launch {
                articleDao.updateReadingProgress(article.id, index)
            }
            if (_readTranslatedVoice.value && _translationResult.value != null) {
                val transResult = _translationResult.value!!
                speechManager.setLanguageByCode(transResult.targetLanguageCode)
                val transLine = transResult.lineByLine.getOrNull(index)?.second ?: lines[index]
                speechManager.speak(transLine, "line_$index")
            } else {
                speechManager.setLanguageByCode(article.languageCode)
                speechManager.speak(lines[index], "line_$index")
            }
        }
    }

    fun nextLine() {
        val article = _selectedArticle.value ?: return
        val lines = article.getLines()
        val next = _currentLineIndex.value + 1
        if (next < lines.size) {
            readLine(next)
        }
    }

    fun previousLine() {
        val prev = (_currentLineIndex.value - 1).coerceAtLeast(0)
        readLine(prev)
    }

    fun toggleAutoAdvance() {
        _autoAdvance.value = !_autoAdvance.value
    }

    fun toggleAutoScroll() {
        _autoScroll.value = !_autoScroll.value
    }

    fun toggleDualLanguage() {
        _showDualLanguage.value = !_showDualLanguage.value
    }

    fun toggleReadTranslatedVoice() {
        _readTranslatedVoice.value = !_readTranslatedVoice.value
    }

    fun setSpeechSpeed(speed: Float) {
        speechManager.setSpeed(speed)
    }

    fun setLanguage(languageCode: String) {
        speechManager.setLanguageByCode(languageCode)
    }

    // Voice selection & tuning controls
    fun openVoiceSelectionDialog() {
        _showVoiceSelectionDialog.value = true
    }

    fun closeVoiceSelectionDialog() {
        _showVoiceSelectionDialog.value = false
        speechManager.stop()
    }

    fun selectVoice(voice: SystemVoiceInfo) {
        speechManager.setVoice(voice)
    }

    fun previewVoice(voice: SystemVoiceInfo, customText: String? = null) {
        speechManager.previewVoice(voice, customText)
    }

    fun stopVoicePreview() {
        speechManager.stop()
    }

    fun setVoicePitch(pitch: Float) {
        speechManager.setPitch(pitch)
    }

    // Translation methods
    fun translateCurrentArticle(targetLangCode: String, promptInstruction: String) {
        val article = _selectedArticle.value ?: articles.value.firstOrNull() ?: return
        _translationResult.value = TranslatorEngine.translateArticleContent(article, targetLangCode, promptInstruction)
    }

    fun selectVisualTopic(topic: VisualTopic) {
        _activeVisualTopic.value = topic
        _currentTab.value = AppTab.VISUALS
    }

    fun updateSummaryConfig(config: SummaryConfig) {
        _summaryConfig.value = config
    }

    fun updateSummaryLength(length: SummaryLength) {
        _summaryConfig.value = _summaryConfig.value.copy(length = length)
    }

    fun updateSummaryTone(tone: SummaryTone) {
        _summaryConfig.value = _summaryConfig.value.copy(tone = tone)
    }

    fun updateSummaryFocus(focus: SummaryFocus) {
        _summaryConfig.value = _summaryConfig.value.copy(focus = focus)
    }

    fun updateSummaryLanguage(lang: String) {
        _summaryConfig.value = _summaryConfig.value.copy(targetLanguage = lang)
    }

    fun generateSummaryForCurrent(config: SummaryConfig? = null) {
        val article = _selectedArticle.value ?: articles.value.firstOrNull() ?: return
        val activeConfig = config ?: _summaryConfig.value
        _summaryConfig.value = activeConfig
        _isSummarizing.value = true
        _summaryError.value = null
        _showSummarySheet.value = true

        viewModelScope.launch {
            try {
                // Call Gemini 3.8 Flash via Firebase AI Logic
                val summary = GeminiSummaryService.generateSummary(article, activeConfig)
                _summaryResult.value = summary
                _summaryError.value = null
            } catch (e: TimeoutCancellationException) {
                Log.e("ReadAloudViewModel", "Gemini summary timed out, using fallback", e)
                val fallback = TextSummarizer.generateSummary(article, activeConfig)
                _summaryResult.value = fallback.copy(source = "Local Text Analysis (Gemini timed out)")
                _summaryError.value = "Gemini AI timed out after 60s. Displaying fallback summary."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ReadAloudViewModel", "Gemini AI error, using fallback: ${e.message}", e)
                val fallback = TextSummarizer.generateSummary(article, activeConfig)
                _summaryResult.value = fallback.copy(source = "Local Analysis (${e.localizedMessage ?: "AI unavailable"})")
                _summaryError.value = "Gemini API: ${e.localizedMessage ?: "Service error"}. Showing local analysis."
            } finally {
                _isSummarizing.value = false
            }
        }
    }

    fun generateLocalSummaryOnly() {
        val article = _selectedArticle.value ?: articles.value.firstOrNull() ?: return
        val activeConfig = _summaryConfig.value
        _isSummarizing.value = false
        _summaryError.value = null
        val summary = TextSummarizer.generateSummary(article, activeConfig)
        _summaryResult.value = summary
    }

    fun readSummaryAloud() {
        val summary = _summaryResult.value ?: return
        val speechText = buildString {
            append(summary.title).append(". ")
            append(summary.executiveSummary).append(". ")
            if (summary.keyTakeaways.isNotEmpty()) {
                append("Key takeaways: ")
                summary.keyTakeaways.forEach { append(it).append(". ") }
            }
        }
        val lang = when (_summaryConfig.value.targetLanguage.lowercase()) {
            "spanish" -> "es"
            "french" -> "fr"
            "german" -> "de"
            "hindi" -> "hi"
            "japanese" -> "ja"
            else -> _selectedArticle.value?.languageCode ?: "en"
        }
        speechManager.setLanguageByCode(lang)
        speechManager.speak(speechText, "summary_reading")
        _isReadingSummary.value = true
    }

    fun stopReadingSummary() {
        speechManager.stop()
        _isReadingSummary.value = false
    }

    fun closeSummarySheet() {
        _showSummarySheet.value = false
        stopReadingSummary()
    }

    fun saveSummaryAsNote() {
        val summary = _summaryResult.value ?: return
        val article = _selectedArticle.value ?: return
        val noteContent = buildString {
            append("AI SUMMARY (${summary.source}):\n")
            append(summary.executiveSummary)
            append("\n\nKEY TAKEAWAYS:\n")
            summary.keyTakeaways.forEach { append("• $it\n") }
            if (summary.keyConcepts.isNotEmpty()) {
                append("\nKEY CONCEPTS:\n")
                summary.keyConcepts.forEach { append("• $it\n") }
            }
            append("\nREADING STATS: ${summary.wordCount} words, ${summary.estimatedReadMinutes} min, Level: ${summary.difficultyLevel}")
        }
        val note = Note(
            id = UUID.randomUUID().toString(),
            articleId = article.id,
            articleTitle = article.title,
            lineIndex = 0,
            quote = article.title,
            noteText = noteContent,
            tag = "AI Summary",
            colorHex = "#10B981"
        )
        viewModelScope.launch {
            noteDao.insertNote(note)
        }
    }

    // Word Lookup & Inspection
    fun inspectWord(word: String, lineText: String) {
        val article = _selectedArticle.value
        val vocab = TextSummarizer.lookupWord(word, lineText, article)
        _inspectedWord.value = vocab
        _showWordLookupDialog.value = true

        // Automatically trigger AI enrichment for rich synonyms & antonyms in background
        enrichInspectedWordWithAi(word, lineText, article)
    }

    fun enrichInspectedWordWithAi(
        word: String? = null,
        lineText: String? = null,
        article: Article? = null
    ) {
        val targetWord = word ?: _inspectedWord.value?.word ?: return
        val targetContext = lineText ?: _inspectedWord.value?.exampleSentence ?: ""
        val targetArticle = article ?: _selectedArticle.value

        viewModelScope.launch {
            _isEnrichingWord.value = true
            try {
                val enriched = GeminiVocabularyService.enrichWordWithAi(targetWord, targetContext, targetArticle)
                // If the user hasn't closed the dialog or inspected another word
                if (_inspectedWord.value?.word.equals(targetWord, ignoreCase = true)) {
                    _inspectedWord.value = enriched
                }
            } catch (e: Exception) {
                Log.d("ReadAloudViewModel", "Word enrichment used offline lexicon: ${e.message}")
            } finally {
                _isEnrichingWord.value = false
            }
        }
    }

    // Vocabulary Builder: Long-press to save word directly to personal list with definition & context
    fun quickSaveWordToVocabulary(word: String, lineText: String) {
        val article = _selectedArticle.value
        val vocab = TextSummarizer.lookupWord(word, lineText, article)
        viewModelScope.launch {
            vocabularyDao.insertWord(vocab)
            _inspectedWord.value = vocab
            _showWordLookupDialog.value = true
            enrichInspectedWordWithAi(word, lineText, article)
        }
    }

    // Scanner Bottom Sheet Controls
    fun openScannerSheet() {
        _scanError.value = null
        _extractedScanResult.value = null
        _showScannerSheet.value = true
    }

    fun closeScannerSheet() {
        _showScannerSheet.value = false
        _isScanning.value = false
    }

    fun scanPageBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanError.value = null
            try {
                val result = GeminiVisionService.extractTextFromPageImage(bitmap)
                _extractedScanResult.value = result
            } catch (e: TimeoutCancellationException) {
                Log.e("ReadAloudViewModel", "Gemini vision scan timed out", e)
                _scanError.value = "Gemini AI scan timed out after 60s. You can try again or use a sample scan below."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ReadAloudViewModel", "Gemini vision scan failed", e)
                _scanError.value = "Scanning error: ${e.localizedMessage ?: "Could not extract text"}. Try a sample below or snap again."
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun useSampleScan(sample: ExtractedScanResult) {
        _extractedScanResult.value = sample
        _scanError.value = null
    }

    fun processExtractedScanAndOpen(result: ExtractedScanResult, action: ScanAction) {
        val article = Article(
            id = UUID.randomUUID().toString(),
            title = result.title.ifBlank { "Scanned Material" },
            author = result.author.ifBlank { "Print Scan" },
            source = result.source.ifBlank { "Camera / Gallery" },
            category = result.category.ifBlank { "News" },
            languageCode = result.languageCode.ifBlank { "en" },
            languageName = result.languageName.ifBlank { "English" },
            content = result.content,
            lastReadLineIndex = 0
        )

        viewModelScope.launch {
            articleDao.insertArticle(article)
            closeScannerSheet()
            openArticle(article, 0)

            when (action) {
                ScanAction.READ_ALOUD -> {
                    // Start reading aloud from line 0
                    readLine(0)
                }
                ScanAction.TRANSLATE -> {
                    // Open Reader with dual language active
                    _showDualLanguage.value = true
                    _readTranslatedVoice.value = true
                }
                ScanAction.SUMMARISE -> {
                    // Trigger Gemini AI summary
                    generateSummaryForCurrent()
                }
                ScanAction.OPEN -> {
                    // Just opened in reader
                }
            }
        }
    }

    fun closeWordLookupDialog() {
        _showWordLookupDialog.value = false
        _inspectedWord.value = null
    }

    fun pronounceWord(word: String, languageCode: String? = null) {
        val lang = languageCode ?: _selectedArticle.value?.languageCode ?: "en"
        speechManager.setLanguageByCode(lang)
        speechManager.speak(word, "word_pronounce")
    }

    fun saveInspectedWord() {
        val word = _inspectedWord.value ?: return
        viewModelScope.launch {
            vocabularyDao.insertWord(word)
            _showWordLookupDialog.value = false
        }
    }

    fun updateWordMastery(wordId: String, status: String) {
        viewModelScope.launch {
            vocabularyDao.updateMastery(wordId, status)
        }
    }

    fun deleteWord(word: VocabularyWord) {
        viewModelScope.launch {
            vocabularyDao.deleteWord(word)
        }
    }

    // Notes
    fun openAddNoteDialog(lineIndex: Int? = null, quote: String? = null) {
        if (lineIndex != null && quote != null) {
            _noteTargetLine.value = Pair(lineIndex, quote)
        } else {
            val article = _selectedArticle.value
            if (article != null) {
                val lines = article.getLines()
                val currentIdx = _currentLineIndex.value.coerceIn(0, (lines.size - 1).coerceAtLeast(0))
                val currentQuote = lines.getOrNull(currentIdx) ?: article.title
                _noteTargetLine.value = Pair(currentIdx, currentQuote)
            } else {
                _noteTargetLine.value = null
            }
        }
        _showAddNoteDialog.value = true
    }

    fun closeAddNoteDialog() {
        _showAddNoteDialog.value = false
        _noteTargetLine.value = null
    }

    fun addNote(text: String, tag: String, colorHex: String) {
        val article = _selectedArticle.value
        val target = _noteTargetLine.value
        val note = Note(
            id = UUID.randomUUID().toString(),
            articleId = article?.id,
            articleTitle = article?.title ?: "Quick Note",
            lineIndex = target?.first,
            quote = target?.second,
            noteText = text,
            tag = tag,
            colorHex = colorHex
        )
        viewModelScope.launch {
            noteDao.insertNote(note)
            closeAddNoteDialog()
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    // Articles management
    fun openAddArticleDialog() {
        _showAddArticleDialog.value = true
    }

    fun closeAddArticleDialog() {
        _showAddArticleDialog.value = false
    }

    fun addNewArticle(
        title: String,
        author: String,
        source: String,
        category: String,
        languageCode: String,
        languageName: String,
        content: String
    ) {
        val article = Article(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Untitled Reading" },
            author = author.ifBlank { "Self Added" },
            source = source.ifBlank { "Pasted Text" },
            category = category,
            languageCode = languageCode,
            languageName = languageName,
            content = content,
            lastReadLineIndex = 0
        )
        viewModelScope.launch {
            articleDao.insertArticle(article)
            _showAddArticleDialog.value = false
            openArticle(article, 0)
        }
    }

    fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            articleDao.updateFavorite(article.id, !article.isFavorite)
        }
    }

    fun deleteArticle(article: Article) {
        viewModelScope.launch {
            articleDao.deleteArticle(article)
            if (_selectedArticle.value?.id == article.id) {
                _selectedArticle.value = null
                _currentTab.value = AppTab.LIBRARY
            }
        }
    }

    fun setLanguageFilter(lang: String) {
        _selectedLanguageFilter.value = lang
    }

    fun setCategoryFilter(cat: String) {
        _selectedCategoryFilter.value = cat
    }

    fun setLightBackgroundOption(option: LightBackgroundOption) {
        _lightBackgroundOption.value = option
        prefs.edit().putString("bg_light_color", option.id).apply()
    }

    fun openColorPicker() {
        _showColorPicker.value = true
    }

    fun closeColorPicker() {
        _showColorPicker.value = false
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.shutdown()
    }
}
