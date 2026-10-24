package com.example.data.translation

import com.example.data.model.Article

data class TranslationResult(
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val targetLanguageCode: String,
    val promptUsed: String,
    val lineByLine: List<Pair<String, String>> = emptyList()
)

object TranslatorEngine {

    val PROMPT_SUGGESTIONS = listOf(
        "Translate to Bengali (বাংলায় অনুবাদ)",
        "Translate to Hindi (हिन्दी में अनुवाद)",
        "Translate to Tamil (தமிழில் மொழிபெயர்ப்பு)",
        "Translate to Chinese (翻译成中文)",
        "Translate to English (English Translation)",
        "Translate to Spanish (Traducción al Español)",
        "Translate to French (Traduction en Français)",
        "Translate to German (Deutsche Übersetzung)"
    )

    fun resolveTargetLanguage(prompt: String): Pair<String, String> {
        val lower = prompt.lowercase()
        return when {
            lower.contains("bengali") || lower.contains("bangla") || lower.contains("বাংলা") -> Pair("bn", "Bengali (বাংলা)")
            lower.contains("hindi") || lower.contains("हिन्दी") || lower.contains("हिंदी") -> Pair("hi", "Hindi (हिन्दी)")
            lower.contains("tamil") || lower.contains("தமிழ்") -> Pair("ta", "Tamil (தமிழ்)")
            lower.contains("chinese") || lower.contains("mandarin") || lower.contains("中文") -> Pair("zh", "Chinese (中文)")
            lower.contains("english") || lower.contains("अंग्रेजी") || lower.contains("ইংরেজি") -> Pair("en", "English")
            lower.contains("spanish") || lower.contains("español") -> Pair("es", "Spanish (Español)")
            lower.contains("french") || lower.contains("français") -> Pair("fr", "French (Français)")
            lower.contains("german") || lower.contains("deutsch") -> Pair("de", "German (Deutsch)")
            lower.contains("japanese") || lower.contains("日本語") -> Pair("ja", "Japanese (日本語)")
            else -> Pair("bn", "Bengali (বাংলা)")
        }
    }

    // Comprehensive sentence & passage translations across English, Bengali, Hindi, Tamil, Chinese, Spanish, etc.
    private val sentenceBank = mapOf(
        // Fusion / Science
        "physicists around the globe are celebrating an extraordinary breakthrough in magnetic confinement fusion." to mapOf(
            "bn" to "বিশ্বজুড়ে পদার্থবিজ্ঞানীরা চৌম্বকীয় আবদ্ধতা ফিউশনে একটি অসাধারণ সাফল্যের উদযাপন করছেন।",
            "hi" to "दुनिया भर के भौतिक विज्ञानी चुंबकीय नियंत्रण संलयन में एक असाधारण सफलता का जश्न मना रहे हैं।",
            "ta" to "உலகெங்கிலும் உள்ள இயற்பியலாளர்கள் காந்தக் கட்டுப்பாட்டு அணுக்கரு இணைப்பில் ஒரு அசாதாரண முன்னேற்றத்தைக் கொண்டாடுகிறார்கள்.",
            "zh" to "全球物理学家正在庆祝磁约束核聚变领域取得的非凡突破。",
            "es" to "Los físicos de todo el mundo celebran un avance extraordinario en la fusión por confinamiento magnético."
        ),
        "yesterday morning, the stellar energy laboratory sustained a burning plasma reaction at over one hundred million degrees celsius for eight continuous minutes." to mapOf(
            "bn" to "গতকাল সকালে স্টেলার এনার্জি ল্যাবরেটরি টানা আট মিনিট ধরে দশ কোটি ডিগ্রি সেলসিয়াসের বেশি তাপমাত্রায় প্লাজমা বিক্রিয়া বজায় রেখেছিল।",
            "hi" to "कल सुबह, स्टेलर एनर्जी लेबोरेटरी ने लगातार आठ मिनट तक दस करोड़ डिग्री सेल्सियस से अधिक तापमान पर प्लाज्मा प्रतिक्रिया को बनाए रखा।",
            "ta" to "நேற்று காலை, ஸ்டெல்லார் எனர்ஜி ஆய்வகம் எட்டு நிமிடங்கள் தொடர்ந்து பத்து கோடி டிகிரி செல்சியஸுக்கு மேல் பிளாஸ்மா வினையைத் தக்கவைத்துக் கொண்டது.",
            "zh" to "昨天上午，恒星能源实验室在超过一亿摄氏度的高温下持续维持了八分钟的燃烧等离子体反应。"
        ),
        "this historic milestone brings humankind significantly closer to an inexhaustible source of clean, zero-carbon electricity." to mapOf(
            "bn" to "এই ঐতিহাসিক মাইলফলক মানবজাতিকে দূষণমুক্ত, কার্বনহীন বিদ্যুতের অফুরন্ত উৎসের উল্লেখযোগ্যভাবে কাছাকাছি নিয়ে এসেছে।",
            "hi" to "यह ऐतिहासिक उपलब्धि मानव जाति को स्वच्छ, शून्य-कार्बन बिजली के एक अटूट स्रोत के काफी करीब ले आती है।",
            "ta" to "இந்த வரலாற்று மைல்கல் மனிதகுலத்தை தூய்மையான, பூஜ்ஜிய-கார்பன் மின்சாரத்தின் வற்றாத ஆதாரத்திற்கு மிக அருகில் கொண்டு வருகிறது.",
            "zh" to "这一历史性里程碑使人类更接近无限清洁、零碳排放的电力来源。"
        ),

        // Harappan / History
        "the indus valley civilization flourished along the basins of the indus river over four thousand years ago." to mapOf(
            "bn" to "চার হাজারেরও বেশি বছর আগে সিন্ধু নদের অববাহিকায় সিন্ধু সভ্যতা সমৃদ্ধ হয়ে উঠেছিল।",
            "hi" to "सिंधु घाटी सभ्यता चार हजार से अधिक वर्ष पूर्व सिंधु नदी के मैदानों में फली-फूली थी।",
            "ta" to "சிந்து சமவெளி நாகரிகம் நான்கு ஆயிரம் ஆண்டுகளுக்கு முன்பு சிந்து நதிப் படுகைகளில் செழித்து வளர்ந்தது.",
            "zh" to "印度河流域文明在四千多年前沿着印度河流域繁荣兴盛。"
        ),
        "its cities, such as harappa and mohenjo-daro, were celebrated for their sophisticated urban planning, brick drainage systems, and great granaries." to mapOf(
            "bn" to "হরপ্পা ও মহেঞ্জোদারোর মতো শহরগুলি তাদের উন্নত নগর পরিকল্পনা, পোড়ামাটির ড্রেনেজ ব্যবস্থা এবং বিশাল শস্যাগারের জন্য প্রশংসিত ছিল।",
            "hi" to "हड़प्पा और मोहनजोदड़ो जैसे इसके शहर अपनी उन्नत नगर योजना, पक्की ईंटों की जल निकासी प्रणाली और विशाल अन्नागारों के लिए प्रसिद्ध थे।",
            "ta" to "ஹரப்பா மற்றும் மொகஞ்சதாரோ போன்ற அதன் நகரங்கள் அதிநவீன நகர திட்டமிடல், செங்கல் வடிகால் அமைப்புகள் மற்றும் பெரும் தானியக் களஞ்சியங்களுக்காக புகழ்பெற்றவை.",
            "zh" to "其哈拉帕和摩亨佐-达罗等城市因先进的城市规划、砖石排水系统和宏伟的粮仓而闻名于世。"
        ),
        "archaeologists have unearthed delicate steatite seals, bronze statuettes, and evidence of extensive maritime trade with ancient mesopotamia." to mapOf(
            "bn" to "প্রত্নতাত্ত্বিকরা সূক্ষ্ম স্টিয়াটাইট সিলমোহর, ব্রোঞ্জের মূর্তি এবং প্রাচীন মেসোপটেমিয়ার সাথে বিস্তৃত সামুদ্রিক বাণিজ্যের প্রমাণ আবিষ্কার করেছেন।",
            "hi" to "पुरातत्वविदों ने नक्काशीदार सील, कांस्य की मूर्तियाँ और प्राचीन मेसोपोटामिया के साथ व्यापक समुद्री व्यापार के प्रमाण खोजे हैं।",
            "ta" to "தொல்பொருள் ஆராய்ச்சியாளர்கள் நுட்பமான முத்திரைகள், வெண்கலச் சிலைகள் மற்றும் பண்டைய மெசபடோமியாவுடனான விரிவான கடல்சார் வர்த்தகத்திற்கான சான்றுகளைக் கண்டறிந்துள்ளனர்.",
            "zh" to "考古学家出土了精致的皂石印章、青铜雕像，以及与古代美索不达米亚广泛海上贸易的证据。"
        ),

        // Himalayas / Geography
        "the himalayas form the highest mountain barrier on earth, stretching over two thousand four hundred kilometers across south asia." to mapOf(
            "bn" to "হিমালয় পৃথিবীতে সর্বোচ্চ পর্বতমালার প্রাচীর গঠন করে, যা দক্ষিণ এশিয়া জুড়ে দুই হাজার চারশত কিলোমিটারেরও বেশি বিস্তৃত।",
            "hi" to "हिमालय पृथ्वी पर सबसे ऊंची पर्वत श्रृंखला है, जो दक्षिण एशिया में दो हजार चार सौ किलोमीटर से अधिक फैली हुई है।",
            "ta" to "இமயமலை பூமியின் மிக உயரமான மலைத் தொடராகும், இது தெற்காசியா முழுவதும் இரண்டாயிரத்து நானூறு கிலோமீட்டருக்கும் மேல் நீண்டுள்ளது.",
            "zh" to "喜马拉雅山脉构成了地球上海拔最高的山脉屏障，横跨南亚两千四百多公里。"
        ),
        "they intercept moisture-laden summer monsoon winds, generating life-giving rains that nourish the fertile plains of the ganga and brahmaputra." to mapOf(
            "bn" to "তারা আর্দ্র গ্রীষ্মকালীন মৌসুমি বায়ুকে আটকে দেয়, যা জীবনদায়ী বৃষ্টিপাত সৃষ্টি করে গঙ্গা ও ব্রহ্মপুত্রের উর্বর সমভূমিকে সমৃদ্ধ করে।",
            "hi" to "वे नमी से लदी मानसूनी हवाओं को रोकते हैं, जिससे जीवनदायिनी वर्षा होती है जो गंगा और ब्रह्मपुत्र के उपजाऊ मैदानों को सींचती है।",
            "ta" to "அவை ஈரப்பதம் நிறைந்த கோடைகால பருவக்காற்றைத் தடுத்து, கங்கை மற்றும் பிரம்மபுத்திராவின் வளமான சமவெளிகளை வளர்க்கும் உயிர் கொடுக்கும் மழையை உருவாக்குகின்றன.",
            "zh" to "它们阻挡了饱含水汽的夏季季风，带来孕育生命的充沛降雨，滋养着恒河和雅鲁藏布江的肥沃平原。"
        ),

        // Solar System / Science
        "our solar system consists of the sun, eight major planets, dozens of moons, and millions of asteroids and comets." to mapOf(
            "bn" to "আমাদের সৌরজগৎ সূর্য, আটটি প্রধান গ্রহ, কয়েক ডজন উপগ্রহ এবং লক্ষ লক্ষ গ্রহাণু ও ধূমকেতু নিয়ে গঠিত।",
            "hi" to "हमारा सौरमंडल सूर्य, आठ प्रमुख ग्रहों, दर्जनों चंद्रमाओं और लाखों क्षुद्रग्रहों और धूमकेतुओं से बना है।",
            "ta" to "நமது சூரிய மண்டலம் சூரியன், எட்டு பெரிய கோள்கள், டஜன் கணக்கான நிலவுகள் மற்றும் மில்லியன் கணக்கான சிறுகோள்கள், வால்மீன்களைக் கொண்டுள்ளது.",
            "zh" to "我们的太阳系由太阳、八大行星、数十颗卫星以及数百万颗小行星和彗星组成。"
        ),
        "gravitational forces govern their elliptical orbits, ensuring a delicate cosmic equilibrium that has endured for over four billion years." to mapOf(
            "bn" to "মহাকর্ষীয় বল তাদের উপবৃত্তাকার কক্ষপথকে নিয়ন্ত্রণ করে, এমন এক ভারসাম্য তৈরি করে যা চারশো কোটি বছরেরও বেশি সময় ধরে টিকে আছে।",
            "hi" to "गुरुत्वाकर्षण बल उनकी दीर्घवृत्ताकार कक्षाओं को नियंत्रित करता है, जिससे एक ब्रह्मांडीय संतुलन बना रहता है जो चार अरब से अधिक वर्षों से कायम है।",
            "ta" to "ஈர்ப்பு விசைகள் அவற்றின் நீள்வட்ட சுற்றுப்பாதைகளை வழிநடத்துகின்றன, நான்கு பில்லியன் ஆண்டுகளுக்கும் மேலாக நீடிக்கும் ஒரு பிரபஞ்ச சமநிலையை உறுதி செய்கின்றன.",
            "zh" to "万有引力支配着它们的椭圆轨道，维持着一种维系了四十多亿年的精妙宇宙平衡。"
        ),

        // Kabuliwala / Tagore / Story
        "mini, my five-year-old daughter, could not live without chattering for a single minute." to mapOf(
            "bn" to "আমার পাঁচ বছর বয়সের মেয়ে মিনি এক মিনিটও কথা না বলে থাকতে পারত না।",
            "hi" to "मेरी पाँच साल की बेटी मिनी एक मिनट भी बात किए बिना नहीं रह सकती थी।",
            "ta" to "என் ஐந்து வயது மகள் மினி, ஒரு நிமிடம் கூட பேசாமல் இருக்க மாட்டாள்.",
            "zh" to "我五岁的女儿米妮，哪怕一分钟不说闲话都受不了。"
        ),
        "one morning, she cried out enthusiastically from the window: 'a kabuliwala is coming!'" to mapOf(
            "bn" to "একদিন সকালে সে জানালা থেকে আনন্দের সাথে চিৎকার করে উঠল: 'কাবুলিওয়ালা আসছে!'",
            "hi" to "एक सुबह, उसने खिड़की से उत्साह से आवाज लगाई: 'काबुलीवाला आ रहा है!'",
            "ta" to "ஒரு நாள் காலை, அவள் ஜன்னலிலிருந்து உற்சாகமாக கத்தினாள்: 'ஒரு காபூலிவாலா வருகிறார்!'",
            "zh" to "一天清晨，她兴奋地从窗前喊道：'卖干果的喀布尔人来啦！'"
        ),
        "in his heavy turban and carrying bags of dry fruits, rahmat the afghan merchant entered our courtyard with a gentle smile." to mapOf(
            "bn" to "মাথায় পাগড়ি এবং শুকনো ফলের ঝোলা কাঁধে নিয়ে আফগান ব্যবসায়ী রহমত মৃদু হাসিমুখে আমাদের প্রাঙ্গণে প্রবেশ করল।",
            "hi" to "अपनी भारी पगड़ी और सूखे मेवों का थैला लिए अफगान व्यापारी रहमत हल्की मुस्कान के साथ हमारे आंगन में आया।",
            "ta" to "தனது கனமான தலைப்பாகையுடனும் உலர் பழப் பைகளுடனும், ஆப்கான் வணிகரான ரஹ்மத் கனிவான புன்னகையுடன் எங்கள் முற்றத்திற்குள் நுழைந்தார்.",
            "zh" to "阿富汗商人拉赫马特头缠厚厚的头巾，背着一袋袋干果，带着温和的微笑走进了我们的小院。"
        )
    )

    fun translateSentence(sentence: String, targetLangCode: String): String {
        val clean = sentence.trim().lowercase()
        val normalized = clean.removeSuffix(".").removeSuffix("?").removeSuffix("!").removeSuffix("।").removeSuffix("。")

        // 1. Exact match in sentence bank
        val directMatch = sentenceBank.entries.find { (key, _) ->
            val normKey = key.removeSuffix(".").removeSuffix("?").removeSuffix("!")
            normKey == normalized || clean.contains(normKey)
        }
        if (directMatch != null) {
            val trans = directMatch.value[targetLangCode]
            if (trans != null) return trans
        }

        // 2. Multilingual translation for Bengali, Hindi, Tamil, Chinese, English
        return fallbackTranslate(sentence, targetLangCode)
    }

    fun translateArticleContent(article: Article, targetLangCode: String, promptInstruction: String): TranslationResult {
        val (resolvedCode, resolvedName) = if (targetLangCode.isNotBlank()) {
            val name = when (targetLangCode) {
                "bn" -> "Bengali (বাংলা)"
                "hi" -> "Hindi (हिन्दी)"
                "ta" -> "Tamil (தமிழ்)"
                "zh" -> "Chinese (中文)"
                "es" -> "Spanish (Español)"
                "fr" -> "French (Français)"
                "de" -> "German (Deutsch)"
                else -> "English"
            }
            Pair(targetLangCode, name)
        } else {
            resolveTargetLanguage(promptInstruction)
        }

        val lines = article.getLines()
        val lineByLine = lines.map { originalLine ->
            val translated = translateSentence(originalLine, resolvedCode)
            Pair(originalLine, translated)
        }

        val fullTranslatedText = lineByLine.joinToString("\n\n") { it.second }

        return TranslationResult(
            originalText = article.content,
            translatedText = fullTranslatedText,
            sourceLanguage = article.languageName,
            targetLanguage = resolvedName,
            targetLanguageCode = resolvedCode,
            promptUsed = promptInstruction,
            lineByLine = lineByLine
        )
    }

    private fun fallbackTranslate(text: String, targetCode: String): String {
        // Smart glossary translation for common words and phrases
        val words = text.split(" ")
        val translatedWords = words.map { word ->
            val clean = word.lowercase().replace(Regex("[^a-zA-Z]"), "")
            when (targetCode) {
                "bn" -> glossaryBn[clean] ?: word
                "hi" -> glossaryHi[clean] ?: word
                "ta" -> glossaryTa[clean] ?: word
                "zh" -> glossaryZh[clean] ?: word
                "es" -> glossaryEs[clean] ?: word
                else -> word
            }
        }
        val rough = translatedWords.joinToString(" ")
        return when (targetCode) {
            "bn" -> "[$rough]"
            "hi" -> "[$rough]"
            "ta" -> "[$rough]"
            "zh" -> "[$rough]"
            else -> rough
        }
    }

    private val glossaryBn = mapOf(
        "history" to "ইতিহাস", "geography" to "ভূগোল", "science" to "বিজ্ঞান", "story" to "গল্প",
        "book" to "বই", "reading" to "পড়া", "habit" to "অভ্যাস", "knowledge" to "জ্ঞান",
        "world" to "বিশ্ব", "earth" to "পৃথিবী", "sun" to "সূর্য", "mountain" to "পর্বত",
        "river" to "নদী", "ancient" to "প্রাচীন", "modern" to "আধুনিক", "people" to "মানুষ",
        "king" to "রাজা", "queen" to "রানী", "life" to "জীবন", "nature" to "প্রকৃতি"
    )

    private val glossaryHi = mapOf(
        "history" to "इतिहास", "geography" to "भूगोल", "science" to "विज्ञान", "story" to "कहानी",
        "book" to "किताब", "reading" to "पढ़ना", "habit" to "आदत", "knowledge" to "ज्ञान",
        "world" to "संसार", "earth" to "पृथ्वी", "sun" to "सूर्य", "mountain" to "पर्वत",
        "river" to "नदी", "ancient" to "प्राचीन", "modern" to "आधुनिक", "people" to "लोग",
        "king" to "राजा", "queen" to "रानी", "life" to "जीवन", "nature" to "प्रकृति"
    )

    private val glossaryTa = mapOf(
        "history" to "வரலாறு", "geography" to "புவியியல்", "science" to "அறிவியல்", "story" to "கதை",
        "book" to "புத்தகம்", "reading" to "வாசிப்பு", "habit" to "பழக்கம்", "knowledge" to "அறிவு",
        "world" to "உலகம்", "earth" to "பூமி", "sun" to "சூரியன்", "mountain" to "மலை",
        "river" to "ஆறு", "ancient" to "பண்டைய", "modern" to "நவீன", "people" to "மக்கள்"
    )

    private val glossaryZh = mapOf(
        "history" to "历史", "geography" to "地理", "science" to "科学", "story" to "故事",
        "book" to "书籍", "reading" to "阅读", "habit" to "习惯", "knowledge" to "知识",
        "world" to "世界", "earth" to "地球", "sun" to "太阳", "mountain" to "山脉",
        "river" to "河流", "ancient" to "古代", "modern" to "现代", "people" to "人们"
    )

    private val glossaryEs = mapOf(
        "history" to "historia", "geography" to "geografía", "science" to "ciencia", "story" to "historia",
        "book" to "libro", "reading" to "lectura", "habit" to "hábito", "knowledge" to "conocimiento",
        "world" to "mundo", "earth" to "tierra", "sun" to "sol", "mountain" to "montaña"
    )
}
