package com.example.data.ai

import com.example.data.model.Article
import com.example.data.model.SummaryResult
import com.example.data.model.VocabularyWord
import java.util.UUID

object TextSummarizer {

    fun generateSummary(article: Article, config: com.example.data.model.SummaryConfig = com.example.data.model.SummaryConfig()): SummaryResult {
        val lines = article.getLines()
        val totalSentences = lines.size
        val words = article.content.split(Regex("\\s+")).filter { it.isNotBlank() }
        val totalWords = words.size
        val estMinutes = (totalWords / 180).coerceAtLeast(1)

        // Difficulty estimation
        val avgWordLength = if (totalWords > 0) words.sumOf { it.length } / totalWords.toDouble() else 0.0
        val avgSentenceLength = if (totalSentences > 0) totalWords / totalSentences.toDouble() else 0.0
        val difficulty = when {
            avgWordLength > 6.0 || avgSentenceLength > 22 -> "Advanced"
            avgWordLength > 4.8 || avgSentenceLength > 14 -> "Intermediate"
            else -> "Accessible"
        }

        // Tone estimation
        val tone = config.tone.label

        // Key takeaways extraction: prioritize first lines, key turning points, and closing lines
        val takeaways = mutableListOf<String>()
        if (lines.isNotEmpty()) {
            takeaways.add(lines.first().removeSuffix(".") + ".")
        }
        if (lines.size > 2) {
            val middleLine = lines[lines.size / 2]
            takeaways.add(middleLine.removeSuffix(".") + ".")
        }
        if (lines.size > 3) {
            val penultimate = lines[lines.size - 2]
            if (!takeaways.contains(penultimate)) {
                takeaways.add(penultimate.removeSuffix(".") + ".")
            }
        }
        if (lines.size > 1) {
            val lastLine = lines.last()
            if (!takeaways.contains(lastLine)) {
                takeaways.add(lastLine.removeSuffix(".") + ".")
            }
        }

        // Key topics extraction (common themes or nouns)
        val stopWords = setOf(
            "the", "and", "a", "an", "in", "of", "to", "for", "with", "on", "at", "by", "from",
            "is", "are", "was", "were", "that", "this", "it", "as", "be", "or", "which",
            "el", "la", "los", "las", "un", "una", "de", "en", "y", "que", "por",
            "le", "la", "les", "un", "une", "des", "du", "et", "en", "est",
            "der", "die", "das", "und", "in", "mit", "von", "zu", "für"
        )
        val candidateTopics = words
            .map { it.lowercase().replace(Regex("[^\\p{L}]"), "") }
            .filter { it.length >= 4 && it !in stopWords }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key.replaceFirstChar { char -> char.uppercase() } }

        val executiveSummary = buildString {
            append("This ${article.category.lowercase()} piece in ${article.languageName} explores ")
            if (candidateTopics.isNotEmpty()) {
                append("themes including ${candidateTopics.take(3).joinToString(", ")}. ")
            } else {
                append("${article.title.lowercase()}. ")
            }
            if (lines.isNotEmpty()) {
                append(lines.first())
                if (lines.size > 1) {
                    append(" ")
                    append(lines.last())
                }
            }
        }

        return SummaryResult(
            title = article.title,
            executiveSummary = executiveSummary,
            keyTakeaways = takeaways.take(4),
            mainTopics = candidateTopics,
            wordCount = totalWords,
            sentenceCount = totalSentences,
            estimatedReadMinutes = estMinutes,
            difficultyLevel = difficulty,
            tone = tone,
            keyConcepts = candidateTopics.take(3).map { "$it: Core theme in reading" },
            source = "Local Text Analysis"
        )
    }

    // Built-in lexicon dictionary for instant word lookup
    private val lexicon = mapOf(
        "breakthrough" to WordDefinition("/ˈbreɪkˌθruː/", "Noun", "A sudden, dramatic, and important discovery or development.", "Un avance importante", "English", "Discovery, Advance, Innovation, Leap", "Setback, Failure, Stagnation"),
        "plasma" to WordDefinition("/ˈplæz.mə/", "Noun", "An ionized state of matter consisting of positive ions and free electrons.", "Plasma (cuarto estado de la materia)", "English", "Ionized gas, Radiant state", "Solid, Liquid"),
        "milestone" to WordDefinition("/ˈmaɪl.stəʊn/", "Noun", "A significant stage or event in the development of something.", "Hito trascendental", "English", "Landmark, Turning point, Achievement, Benchmark", "Regression, Stagnation"),
        "inexhaustible" to WordDefinition("/ˌɪn.ɪɡˈzɔː.stə.bəl/", "Adjective", "Existing in unlimited quantity; incapable of being depleted.", "Inagotable / Infinito", "English", "Endless, Boundless, Infinite, Limitless", "Finite, Limited, Depleted"),
        "radioactive" to WordDefinition("/ˌreɪ.di.əʊˈæk.tɪv/", "Adjective", "Emitting radiation through nuclear decay.", "Radiactivo", "English", "Irradiated, Unstable, Nuclear", "Stable, Inert"),
        "vulnerable" to WordDefinition("/ˈvʌl.nər.ə.bəl/", "Adjective", "Exposed to the possibility of being attacked or harmed.", "Vulnerable", "English", "Susceptible, Exposed, Defenseless, Fragile", "Resilient, Protected, Invulnerable, Strong"),
        "communicative" to WordDefinition("/kəˈmjuː.nɪ.kə.tɪv/", "Adjective", "Willing or eager to talk and disclose information.", "Comunicativo", "English", "Expressive, Responsive, Articulate, Open", "Reticent, Reserved, Taciturn, Secretive"),
        "judgements" to WordDefinition("/ˈdʒʌdʒ.mənts/", "Noun", "Opinions or conclusions formed after careful thought.", "Juicios u opiniones", "English", "Verdict, Assessment, Evaluation, Finding", "Indecision, Uncertainty"),
        "murales" to WordDefinition("/muˈɾa.les/", "Sustantivo", "Pinturas u obras de arte realizadas directamente sobre un muro.", "Murals / Wall paintings", "Español", "Pinturas, Frescos, Arte mural", ""),
        "polícromos" to WordDefinition("/poˈli.kɾo.mos/", "Adjetivo", "Que tiene o está pintado con varios colores.", "Polychrome / Multi-colored", "Español", "Multicolor, Variado, Cromático", "Monocromático, Unicolor"),
        "arqueólogos" to WordDefinition("/aɾ.keˈo.lo.ɣos/", "Sustantivo", "Científicos que estudian las sociedades del pasado.", "Archaeologists", "Español", "Historiadores, Antropólogos", ""),
        "renard" to WordDefinition("/ʁə.naʁ/", "Nom", "Mammifère carnivore à la queue touffue et au pelage roux.", "Fox (Zorro)", "Français", "Goupil, Canidé", ""),
        "apprivoiser" to WordDefinition("/a.pʁi.vwa.ze/", "Verbe", "Créer des liens d'affection, rendre familier.", "To tame / Befriend", "Français", "Domestiquer, Habituer, Charmer", "Effrayer, Éloigner, Sauvage"),
        "invisible" to WordDefinition("/ɛ̃.vi.zibl/", "Adjectif", "Que l'on ne peut pas voir avec les yeux.", "Invisible", "Français", "Imperceptible, Caché, Secret", "Visible, Évident, Manifeste"),
        "tiefsee" to WordDefinition("/ˈtiːfˌzeː/", "Substantiv", "Die dunklen, tiefen Zonen der Weltmeere unter 200 m.", "Deep sea / Abyssal ocean", "Deutsch", "Abgrund, Meerestiefe", "Flachwasser, Oberfläche"),
        "biolumineszenz" to WordDefinition("/ˌbioluminɛsˈt͡sɛnts/", "Substantiv", "Lichterzeugung durch biochemische Reaktionen in Lebewesen.", "Bioluminescence", "Deutsch", "Selbstleuchten, Kaltes Licht", "Dunkelheit"),
        "खगोल" to WordDefinition("/kʰə.ɡoːl/", "संज्ञा", "आकाशीय पिंडों और अंतरिक्ष का अध्ययन (Astronomy).", "Astronomy / Celestial space", "हिन्दी", "अंतरिक्ष, ब्रह्मांड, गगन, नभ", "भूगोल, पृथ्वी"),
        "गणित" to WordDefinition("/ɡə.ɳɪt̪/", "संज्ञा", "संख्याओं, मात्राओं और आकारों का विज्ञान (Mathematics).", "Mathematics", "हिन्दी", "अंकशास्त्र, गणना, हिसाब", ""),
        "धुरी" to WordDefinition("/d̪ʱʊ.ɾiː/", "संज्ञा", "जिस पर कोई वस्तु घूमती है (Axis / Pivot).", "Axis / Pivot point", "हिन्दी", "अक्ष, केंद्र, आधार, मुख्य स्तंभ", "परिधि, किनारा"),
        "四季" to WordDefinition("/ɕi.kʲi/", "名詞", "春・夏・秋・冬の四つの季節。", "Four seasons", "日本語", "春夏秋冬, 季節", ""),
        "美意識" to WordDefinition("/bi.i.ɕi.kʲi/", "名詞", "美しさに対する感じ方や価値観。", "Aesthetic sense", "日本語", "審美眼, 美的情操, センス", "無関心")
    )

    data class WordDefinition(
        val phonetic: String,
        val partOfSpeech: String,
        val definition: String,
        val translation: String,
        val language: String,
        val synonyms: String = "",
        val antonyms: String = ""
    )

    fun lookupWord(
        rawWord: String,
        contextSentence: String = "",
        article: Article? = null
    ): VocabularyWord {
        val cleanWord = rawWord.trim().replace(Regex("[^\\p{L}\\p{M}-]"), "")
        val lowerKey = cleanWord.lowercase()
        val entry = lexicon[lowerKey]

        val phonetic = entry?.phonetic ?: "/${cleanWord.lowercase()}/"
        val partOfSpeech = entry?.partOfSpeech ?: guessPartOfSpeech(cleanWord)
        val definition = entry?.definition ?: "The term '$cleanWord' used in context: \"${contextSentence.take(120)}\""
        val translation = entry?.translation ?: "Vocabulary item in ${article?.languageName ?: "Text"}"
        val synonyms = entry?.synonyms?.ifBlank { defaultSynonymsFor(cleanWord) } ?: defaultSynonymsFor(cleanWord)
        val antonyms = entry?.antonyms?.ifBlank { defaultAntonymsFor(cleanWord) } ?: defaultAntonymsFor(cleanWord)

        return VocabularyWord(
            id = UUID.randomUUID().toString(),
            word = cleanWord.ifBlank { rawWord },
            languageCode = article?.languageCode ?: "en",
            phonetic = phonetic,
            partOfSpeech = partOfSpeech,
            definition = definition,
            exampleSentence = contextSentence.ifBlank { "Used in: ${article?.title ?: "Reading"}" },
            translation = translation,
            targetLanguage = "English",
            masteryStatus = "Learning",
            articleId = article?.id,
            articleTitle = article?.title,
            synonyms = synonyms,
            antonyms = antonyms
        )
    }

    private fun defaultSynonymsFor(word: String): String {
        return when (word.lowercase()) {
            "resilient", "resilience" -> "Tough, Flexible, Enduring, Strong"
            "rapid", "rapidly" -> "Quick, Swift, Fast, Speedy"
            "crucial" -> "Critical, Essential, Vital, Pivotal"
            "profound" -> "Deep, Significant, Intense, Meaningful"
            "monumental" -> "Historic, Massive, Epic, Enormous"
            "sophisticated" -> "Advanced, Refined, Complex, Cultivated"
            "flourish", "flourished" -> "Thrive, Prosper, Bloom, Blossom"
            "delicate" -> "Fragile, Fine, Subtle, Exquisite"
            else -> ""
        }
    }

    private fun defaultAntonymsFor(word: String): String {
        return when (word.lowercase()) {
            "resilient", "resilience" -> "Fragile, Vulnerable, Weak"
            "rapid", "rapidly" -> "Slow, Sluggish, Gradual"
            "crucial" -> "Trivial, Minor, Unimportant, Negligible"
            "profound" -> "Superficial, Shallow, Trivial"
            "monumental" -> "Insignificant, Minor, Petty"
            "sophisticated" -> "Crude, Simple, Primitive"
            "flourish", "flourished" -> "Decline, Wither, Fail, Perish"
            "delicate" -> "Coarse, Robust, Sturdy, Heavy"
            else -> ""
        }
    }

    private fun guessPartOfSpeech(word: String): String {
        return when {
            word.endsWith("ly", ignoreCase = true) -> "Adverb"
            word.endsWith("ing", ignoreCase = true) || word.endsWith("ed", ignoreCase = true) -> "Verb"
            word.endsWith("tion", ignoreCase = true) || word.endsWith("ness", ignoreCase = true) || word.endsWith("ment", ignoreCase = true) -> "Noun"
            word.endsWith("able", ignoreCase = true) || word.endsWith("ous", ignoreCase = true) || word.endsWith("ful", ignoreCase = true) -> "Adjective"
            else -> "Noun / Term"
        }
    }
}
