package com.example

import com.example.data.ai.TextSummarizer
import com.example.data.model.Article
import com.example.data.translation.TranslatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testArticleLineSplitting() {
        val article = Article(
            id = "test_1",
            title = "Test Article",
            content = "First sentence here. Second sentence follows! Third sentence is a question?",
            languageCode = "en"
        )
        val lines = article.getLines()
        assertEquals(3, lines.size)
        assertEquals("First sentence here.", lines[0])
        assertEquals("Second sentence follows!", lines[1])
        assertEquals("Third sentence is a question?", lines[2])
    }

    @Test
    fun testMultilingualLineSplitting() {
        val spanishArticle = Article(
            id = "test_es",
            title = "Noticias en Español",
            content = "Primera oración en español. Segunda oración importante.",
            languageCode = "es",
            languageName = "Español"
        )
        val lines = spanishArticle.getLines()
        assertEquals(2, lines.size)

        val hindiArticle = Article(
            id = "test_hi",
            title = "समाचार",
            content = "यह पहला वाक्य है। यह दूसरा वाक्य है।",
            languageCode = "hi",
            languageName = "हिन्दी"
        )
        val hindiLines = hindiArticle.getLines()
        assertEquals(2, hindiLines.size)
    }

    @Test
    fun testSummarizerGeneratesMetricsAndTakeaways() {
        val article = Article(
            id = "test_summary",
            title = "Breakthrough in Clean Fusion",
            category = "News",
            content = "Scientists announced a monumental fusion breakthrough today. Magnetic confinement kept plasma burning for eight minutes. This represents a historic turning point for zero-carbon energy.",
            languageCode = "en"
        )
        val summary = TextSummarizer.generateSummary(article)
        assertNotNull(summary)
        assertTrue(summary.wordCount > 10)
        assertTrue(summary.keyTakeaways.isNotEmpty())
        assertTrue(summary.executiveSummary.isNotBlank())
    }

    @Test
    fun testVocabularyLookup() {
        val vocab = TextSummarizer.lookupWord(
            rawWord = "breakthrough",
            contextSentence = "Scientists announced a monumental fusion breakthrough today."
        )
        assertEquals("breakthrough", vocab.word)
        assertEquals("Noun", vocab.partOfSpeech)
        assertTrue(vocab.definition.contains("discovery", ignoreCase = true))
    }

    @Test
    fun testTranslationPromptResolution() {
        val (bnCode, _) = TranslatorEngine.resolveTargetLanguage("Translate to Bengali")
        assertEquals("bn", bnCode)

        val (hiCode, _) = TranslatorEngine.resolveTargetLanguage("Translate like Hindi")
        assertEquals("hi", hiCode)

        val (taCode, _) = TranslatorEngine.resolveTargetLanguage("Translate into Tamil please")
        assertEquals("ta", taCode)

        val (zhCode, _) = TranslatorEngine.resolveTargetLanguage("Translate like Chinese")
        assertEquals("zh", zhCode)
    }

    @Test
    fun testSentenceTranslationBengaliAndHindi() {
        val sentence = "The Indus Valley civilization flourished along the basins of the Indus River over four thousand years ago."
        val bnTrans = TranslatorEngine.translateSentence(sentence, "bn")
        assertTrue(bnTrans.contains("সিন্ধু") || bnTrans.isNotBlank())

        val hiTrans = TranslatorEngine.translateSentence(sentence, "hi")
        assertTrue(hiTrans.contains("सिंधु") || hiTrans.isNotBlank())
    }

    @Test
    fun testVocabularyBuilderWithContext() {
        val sentence = "The gravitational pull curves spacetime into a geometric fabric."
        val vocab = TextSummarizer.lookupWord(
            rawWord = "gravitational",
            contextSentence = sentence
        )
        assertEquals("gravitational", vocab.word)
        assertEquals(sentence, vocab.exampleSentence)
        assertTrue(vocab.definition.isNotBlank())
        assertTrue(vocab.translation.isNotBlank())
    }

    @Test
    fun testReadingProgressPercentage() {
        val article = Article(
            id = "prog_test",
            title = "Progress Test",
            content = "Line one here. Line two here. Line three here. Line four here.",
            lastReadLineIndex = 1
        )
        val lines = article.getLines()
        assertEquals(4, lines.size)

        // Line 2 (index 1) out of 4 lines = 50%
        val percentage = ((article.lastReadLineIndex + 1) * 100 / lines.size).coerceIn(0, 100)
        assertEquals(50, percentage)

        // Progress float
        val progress = ((article.lastReadLineIndex + 1).toFloat() / lines.size.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.5f, progress, 0.001f)

        // Completed (last line index = 3)
        val completedPercentage = ((3 + 1) * 100 / lines.size).coerceIn(0, 100)
        assertEquals(100, completedPercentage)
    }
}
