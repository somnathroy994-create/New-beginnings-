package com.example.data.local

import com.example.data.model.Article
import com.example.data.model.Note
import com.example.data.model.VocabularyWord

object PreloadedContent {
    val sampleArticles: List<Article> = listOf(
        // 1. History (English)
        Article(
            id = "art_hist_1",
            title = "Ancient Harappan Civilization & The Indus Valley",
            author = "Prof. Arvind Sharma",
            source = "Historical Archaeology Journal",
            category = "History",
            languageCode = "en",
            languageName = "English",
            content = """The Indus Valley civilization flourished along the basins of the Indus River over four thousand years ago.
Its cities, such as Harappa and Mohenjo-Daro, were celebrated for their sophisticated urban planning, brick drainage systems, and great granaries.
Archaeologists have unearthed delicate steatite seals, bronze statuettes, and evidence of extensive maritime trade with ancient Mesopotamia.
Unlike many contemporary civilizations, excavations have revealed no grand royal palaces or monuments dedicated to warfare.
Instead, historians observe a remarkably egalitarian urban culture centered around sanitation, civic organization, and prosperous commerce.
This monumental Bronze Age society continues to provide crucial insights into human architectural ingenuity.""".trimIndent(),
            lastReadLineIndex = 0,
            isFavorite = true
        ),

        // 2. Stories / Literature (Bengali) - Rabindranath Tagore
        Article(
            id = "art_story_bn",
            title = "রবীন্দ্রনাথ ঠাকুরের কাবুলিওয়ালা (স্মৃতি ও সখ্যতা)",
            author = "রবীন্দ্রনাথ ঠাকুর (Rabindranath Tagore)",
            source = "বাংলা সাহিত্য সংকলন",
            category = "Stories",
            languageCode = "bn",
            languageName = "Bengali (বাংলা)",
            content = """আমার পাঁচ বছর বয়সের ছোট মেয়ে মিনি এক মিনিটও কথা না বলে থাকতে পারত না।
সকালবেলায় যখন আমি আমার লেখার টেবিলে কাজ করছিলাম, মিনি এসে বলল, 'বাবা, রামদয়াল দারোয়ান কাকাকে কৌয়া বলছিল, ও কিছু জানে না না?'
ঠিক এমন সময় পথের দিকে তাকিয়ে সে হঠাৎ পরম আনন্দে চিৎকার করে উঠল: 'কাবুলিওয়ালা! ও কাবুলিওয়ালা!'
ময়লা ঢিলেঢালা পোশাক পরা, মাথায় পাগড়ি বাঁধা এবং কাঁধে বাদাম-কিশমিশের ঝোলা নিয়ে রহমত আফগান আমাদের দরজায় এসে দাঁড়াল।
প্রথমে মিনি তাকে দেখে ভয় পেলেও, রহমতের অমায়িক হাসি এবং উপঢৌকন ধীরে ধীরে তাদের মধ্যে এক অপূর্ব স্নেহের বন্ধন গড়ে তুলেছিল।
দূর আফগানিস্তানের পর্বতমালা ছেড়ে আসা এই ব্যবসায়ীর অন্তরেও ছিল এক পিতা হৃদয়ের ব্যাকুল ভালোবাসা।""".trimIndent(),
            lastReadLineIndex = 0,
            isFavorite = true
        ),

        // 3. Stories / Fables (Hindi) - Panchatantra
        Article(
            id = "art_story_hi",
            title = "पंचतंत्र की अमर कथा: बुद्धिमान खरगोश और शेर",
            author = "विष्णु शर्मा (Vishnu Sharma)",
            source = "पंचतंत्र नीति कथाएँ",
            category = "Stories",
            languageCode = "hi",
            languageName = "Hindi (हिन्दी)",
            content = """एक घने जंगल में भासुरक नाम का एक अत्यंत बलशाली और क्रूर शेर रहता था।
वह प्रतिदिन जंगल के अनेक निर्दोष पशुओं का अकारण शिकार करता था।
सभी पशुओं ने मिलकर एक सभा की और शेर से प्रार्थना की कि वे प्रतिदिन स्वयं एक पशु उसके भोजन के लिए भेजेंगे।
एक दिन एक चतुर नन्हें खरगोश की बारी आई, जिसने अपने अदम्य साहस और तीक्ष्ণ बुद्धि से पूरे जंगल को बचाने का संकल्प लिया।
खरगोश जानबूझकर बहुत देर से पहुँचा और शेर से बोला कि रास्ते में एक दूसरे शेर ने उसे रोक लिया था।
क्रोधित होकर शेर जब कुएं में अपनी ही परछाईं को दूसरा शेर समझकर कूदा, तो बुद्धि ने बाहुबल पर विजय प्राप्त कर ली।""".trimIndent(),
            lastReadLineIndex = 0,
            isFavorite = true
        ),

        // 4. Literature & Wisdom (Tamil) - Thirukkural
        Article(
            id = "art_story_ta",
            title = "திருக்குறள்: வாழ்வியல் நெறிகள் மற்றும் கல்விச் செல்வம்",
            author = "திருவள்ளுவர் (Thiruvalluvar)",
            source = "தமிழ் இலக்கியக் களஞ்சியம்",
            category = "Stories",
            languageCode = "ta",
            languageName = "Tamil (தமிழ்)",
            content = """அகர முதல எழுத்தெல்லாம் ஆதி பகவன் முதற்றே உலகு.
எழுத்துக்கள் எல்லாம் 'அ' கரத்தை முதலாகக் கொண்டுள்ளன; அதுபோல இந்த உலகம் இறைவனை முதலாகக் கொண்டு இயங்குகிறது.
கற்க கசடறக் கற்பவை கற்றபின் நிற்க அதற்குத் தக.
நாம் கற்கும் நூல்களைக் குற்றமறக் கற்க வேண்டும்; கற்ற பிறகு அந்த நல்ல நெறிகளின்படி நமது வாழ்க்கையை அமைத்துக் கொள்ள வேண்டும்.
அறிவினால் ஆகுவது உண்டோ பிறிதின்நோய் தம்நோய்போல் போற்றாக் கடை.
பிற உயிர்களின் துன்பத்தைத் தன் துன்பம் போல் கருதிப் போற்றாவிட்டால், ஒருவர் பெற்ற அறிவினால் என்ன பயன்?
இந்த உலகளாவிய நற்பண்புகள் காலத்தைக் கடந்து மனிதகுலத்திற்கு நல்வழி காட்டுகின்றன.""".trimIndent(),
            lastReadLineIndex = 0,
            isFavorite = true
        ),

        // 5. Stories / Classic (Chinese) - Journey to the West
        Article(
            id = "art_story_zh",
            title = "《西游记》：花果山美猴王出世与水帘洞",
            author = "吴承恩 (Wu Cheng'en)",
            source = "中国古典名著选",
            category = "Stories",
            languageCode = "zh",
            languageName = "Chinese (中文)",
            content = """东胜神洲傲来国有一座名山，叫做花果山，那座山顶上有一块吸收日月精华的仙石。
一日石破天惊，产出一枚石卵，因见风化作一个神通广大的石猴。
石猴在山中与众猴嬉戏，见山涧泉水奔流，便同去探寻源头，发现了一处飞流直下的瀑布。
石猴自告奋勇瞑目蹲身跳进瀑布之中，穿过水浪，赫然见到一座宽敞幽雅的铁板桥与水帘洞天。
洞内石座石床一应俱全，众猴欢天喜地拜其为王，尊称为“美猴王”。
由此开启了一段跨越万水千山、降妖伏魔、求取真经的伟大东方传奇。""".trimIndent(),
            lastReadLineIndex = 0,
            isFavorite = false
        ),

        // 6. Geography & Climate (English & South Asia)
        Article(
            id = "art_geo_1",
            title = "The Mighty Himalayas & The Asian Monsoon Climate",
            author = "Dr. Maya Sen",
            source = "Earth & Geography Review",
            category = "Geography",
            languageCode = "en",
            languageName = "English",
            content = """The Himalayas form the highest mountain barrier on Earth, stretching over two thousand four hundred kilometers across South Asia.
They intercept moisture-laden summer monsoon winds, generating life-giving rains that nourish the fertile plains of the Ganga and Brahmaputra.
Glaciers nestled in high Himalayan valleys act as freshwater towers, sustaining over one billion people downriver.
From the subtropical tea gardens of Darjeeling to the sub-zero peaks of Mount Everest at eight thousand eight hundred and forty-eight meters, climate zones change dramatically within tens of kilometers.
Geologists consider these young fold mountains among the most active tectonic zones on our planet, rising approximately five millimeters each year.""".trimIndent(),
            lastReadLineIndex = 0,
            isFavorite = true
        ),

        // 7. Science & Astronomy (English)
        Article(
            id = "art_sci_1",
            title = "The Solar System: Orbital Dynamics & Cosmic Secrets",
            author = "Dr. Elena Vance",
            source = "Astrophysical Wonders",
            category = "Science",
            languageCode = "en",
            languageName = "English",
            content = """Our solar system consists of the Sun, eight major planets, dozens of moons, and millions of asteroids and comets.
Gravitational forces govern their elliptical orbits, ensuring a delicate cosmic equilibrium that has endured for over four billion years.
Inner terrestrial planets like Earth and Mars possess rocky mantles, whereas outer giants like Jupiter and Saturn boast majestic ring systems and swirling storm clouds.
Modern space telescopes and robotic probes continue to search for subterranean water ice and prebiotic chemistry across distant icy moons such as Europa and Enceladus.
Understanding our celestial neighborhood unlocks fundamental mysteries regarding the origin of water, life, and terrestrial atmosphere.""".trimIndent(),
            lastReadLineIndex = 0,
            isFavorite = true
        )
    )

    val sampleVocabulary: List<VocabularyWord> = listOf(
        VocabularyWord(
            id = "vocab_bn_1",
            word = "সখ্যতা (Sakhyatā)",
            languageCode = "bn",
            phonetic = "/ʃɔkʰːo.t̪a/",
            partOfSpeech = "বিশেষ্য (Noun)",
            definition = "আন্তরিক বন্ধুত্ব, সৌহার্দ্য ও গভীর স্নেহের সম্পর্ক।",
            exampleSentence = "মিনি এবং কাবুলিওয়ালার মধ্যে এক অপূর্ব সখ্যতা গড়ে উঠেছিল।",
            translation = "Friendship / Intimate Companionship",
            targetLanguage = "English",
            masteryStatus = "Learning",
            articleId = "art_story_bn",
            articleTitle = "রবীন্দ্রনাথ ঠাকুরের কাবুলিওয়ালা",
            synonyms = "বন্ধুত্ব, মৈত্রী, সৌহার্দ্য, আত্মীয়তা",
            antonyms = "শত্রুতা, বিবাদ, দূরত্ব, বৈরিতা"
        ),
        VocabularyWord(
            id = "vocab_hi_1",
            word = "पराक्रम (Parākram)",
            languageCode = "hi",
            phonetic = "/pə.ɾɑːk.ɾəm/",
            partOfSpeech = "संज्ञा (Noun)",
            definition = "अदम्य साहस, वीरता और शौर्य का प्रदर्शन।",
            exampleSentence = "खरगोश ने अपनी बुद्धि और पराक्रम से शेर को परास्त किया।",
            translation = "Valor / Heroic Courage",
            targetLanguage = "English",
            masteryStatus = "Reviewing",
            articleId = "art_story_hi",
            articleTitle = "पंचतंत्र: बुद्धिमान खरगोश",
            synonyms = "शौर्य, वीरता, साहस, पुरुषार्थ",
            antonyms = "कायरता, भय, भीरुता, दुर्बलता"
        ),
        VocabularyWord(
            id = "vocab_ta_1",
            word = "கசடற (Kasadara)",
            languageCode = "ta",
            phonetic = "/kɐsɐɖɐrɐ/",
            partOfSpeech = "வினையடை (Adverb)",
            definition = "குற்றமில்லாமல், மிகத் தெளிவாக, சந்தேகமற முழுமையாகக் கற்றல்.",
            exampleSentence = "கற்க கசடறக் கற்பவை கற்றபின் நிற்க அதற்குத் தக.",
            translation = "Flawlessly / Without Error",
            targetLanguage = "English",
            masteryStatus = "Mastered",
            articleId = "art_story_ta",
            articleTitle = "திருக்குறள்",
            synonyms = "குற்றமற, தெளிவாக, பிழையின்றி, செம்மையாக",
            antonyms = "குற்றத்துடன், பிழையாக, குழப்பமாக"
        ),
        VocabularyWord(
            id = "vocab_zh_1",
            word = "神通广大 (Shéntōng Guǎngdà)",
            languageCode = "zh",
            phonetic = "/ʂən tʰʊŋ kʷaŋ ta/",
            partOfSpeech = "成语 (Idiom)",
            definition = "形容本领极其高强，无所不能。",
            exampleSentence = "见风化作一个神通广大的石猴。",
            translation = "Possessing immense supernatural prowess",
            targetLanguage = "English",
            masteryStatus = "Learning",
            articleId = "art_story_zh",
            articleTitle = "《西游记》",
            synonyms = "无所不能, 呼风唤雨, 盖世无双, 出神入化",
            antonyms = "无能为力, 束手无策, 黔驴技穷, 碌碌无为"
        ),
        VocabularyWord(
            id = "vocab_en_1",
            word = "Equilibrium",
            languageCode = "en",
            phonetic = "/ˌiː.kwəˈlɪb.ri.əm/",
            partOfSpeech = "Noun",
            definition = "A state in which opposing forces or influences are balanced.",
            exampleSentence = "Gravitational forces maintain a delicate cosmic equilibrium.",
            translation = "ভারসাম্য (Bengali) / संतुलन (Hindi)",
            targetLanguage = "Bengali",
            masteryStatus = "Mastered",
            articleId = "art_sci_1",
            articleTitle = "The Solar System",
            synonyms = "Balance, Stability, Symmetry, Poise, Equanimity",
            antonyms = "Imbalance, Instability, Disequilibrium, Chaos"
        )
    )

    val sampleNotes: List<Note> = listOf(
        Note(
            id = "note_tagore",
            articleId = "art_story_bn",
            articleTitle = "রবীন্দ্রনাথ ঠাকুরের কাবুলিওয়ালা",
            lineIndex = 5,
            quote = "দূর আফগানিস্তানের পর্বতমালা ছেড়ে আসা এই ব্যবসায়ীর অন্তরেও ছিল এক পিতা হৃদয়ের ব্যাকুল ভালোবাসা।",
            noteText = "Tagore's universal humanist theme: Geographical and national boundaries dissolve before the shared bond of parental love.",
            tag = "Insight",
            colorHex = "#F59E0B"
        ),
        Note(
            id = "note_geo",
            articleId = "art_geo_1",
            articleTitle = "The Mighty Himalayas",
            lineIndex = 1,
            quote = "They intercept moisture-laden summer monsoon winds, generating life-giving rains...",
            noteText = "The orographic effect of the Himalayas is the single most critical driver of agriculture across the Indian subcontinent.",
            tag = "Key Point",
            colorHex = "#38BDF8"
        ),
        Note(
            id = "note_panchatantra",
            articleId = "art_story_hi",
            articleTitle = "पंचतंत्र: बुद्धिमान खरगोश",
            lineIndex = 5,
            quote = "शेर जब कुएं में अपनी ही परछाईं को दूसरा शेर समझकर कूदा, तो बुद्धि ने बाहुबल पर विजय प्राप्त कर ली।",
            noteText = "Wisdom and strategy will always overcome raw physical brute force.",
            tag = "Quote",
            colorHex = "#10B981"
        )
    )
}
