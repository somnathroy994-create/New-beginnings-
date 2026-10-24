package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.TextSummarizer
import com.example.data.model.Article
import com.example.data.model.SummaryConfig
import com.example.data.model.SummaryFocus
import com.example.data.model.SummaryLength
import com.example.data.model.SummaryTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Reading Habit", appName)
    }

    @Test
    fun `verify summary configuration models and default values`() {
        val config = SummaryConfig()
        assertEquals(SummaryLength.BULLETS, config.length)
        assertEquals(SummaryTone.BALANCED, config.tone)
        assertEquals(SummaryFocus.OVERVIEW, config.focus)
        assertEquals("Original", config.targetLanguage)

        val customConfig = config.copy(
            length = SummaryLength.DEEP_DIVE,
            tone = SummaryTone.ANALYTICAL,
            focus = SummaryFocus.THEMES,
            targetLanguage = "French"
        )
        assertEquals(SummaryLength.DEEP_DIVE, customConfig.length)
        assertEquals(SummaryTone.ANALYTICAL, customConfig.tone)
        assertEquals(SummaryFocus.THEMES, customConfig.focus)
        assertEquals("French", customConfig.targetLanguage)
    }

    @Test
    fun `verify structured summary generation with configurable tone and length`() {
        val testArticle = Article(
            id = "test_article_1",
            title = "The Quantum Universe",
            author = "Dr. Jane Doe",
            source = "Scientific Journal",
            category = "Science",
            languageCode = "en",
            languageName = "English",
            content = "Quantum mechanics describes the physical properties of nature at the scale of atoms and subatomic particles. It is the foundation of all quantum physics including quantum chemistry, quantum field theory, and quantum computing. Understanding these principles allows researchers to engineer next-generation semiconductors and quantum cryptography systems.",
            lastReadLineIndex = 0
        )

        val config = SummaryConfig(
            length = SummaryLength.BULLETS,
            tone = SummaryTone.ANALYTICAL,
            focus = SummaryFocus.ACTIONABLE
        )

        val summary = TextSummarizer.generateSummary(testArticle, config)
        assertNotNull(summary)
        assertEquals("The Quantum Universe", summary.title)
        assertTrue(summary.wordCount > 30)
        assertTrue(summary.sentenceCount >= 1)
        assertTrue(summary.keyTakeaways.isNotEmpty())
        assertEquals("Analytical", summary.tone)
    }

    @Test
    fun `verify vocabulary word synonyms and antonyms parsing`() {
        val vocab = com.example.data.model.VocabularyWord(
            id = "test_vocab_1",
            word = "Equilibrium",
            definition = "A state of physical or mental balance.",
            synonyms = "Balance, Stability, Symmetry, Poise",
            antonyms = "Imbalance, Chaos, Instability"
        )

        val synonyms = vocab.getSynonymList()
        val antonyms = vocab.getAntonymList()

        assertEquals(4, synonyms.size)
        assertTrue(synonyms.contains("Balance"))
        assertTrue(synonyms.contains("Stability"))

        assertEquals(3, antonyms.size)
        assertTrue(antonyms.contains("Imbalance"))
        assertTrue(antonyms.contains("Chaos"))
    }

    @Test
    fun `verify word lookup in context yields synonyms and antonyms`() {
        val vocab = TextSummarizer.lookupWord("breakthrough", "The team achieved a monumental breakthrough.")
        assertEquals("breakthrough", vocab.word)
        assertTrue(vocab.getSynonymList().isNotEmpty())
        assertTrue(vocab.getSynonymList().contains("Discovery"))
        assertTrue(vocab.getAntonymList().contains("Setback"))
    }

    @Test
    fun `verify scanned print pages and newspaper samples`() {
        val samples = com.example.data.ai.GeminiVisionService.sampleScans
        assertTrue(samples.isNotEmpty())

        val newspaperSample = samples.find { it.detectedType == "Newspaper Article" }
        assertNotNull(newspaperSample)
        assertTrue(newspaperSample!!.content.length > 50)
        assertTrue(newspaperSample.content.contains("\n"))

        val hindiSample = samples.find { it.languageCode == "hi" }
        assertNotNull(hindiSample)
        assertEquals("Hindi (हिन्दी)", hindiSample!!.languageName)
    }

    @Test
    fun `verify system voices catalogue contains male female and multiple regional accents`() {
        val curatedVoices = com.example.data.tts.SpeechManager.CURATED_VOICES
        assertTrue("Curated voices should not be empty", curatedVoices.isNotEmpty())

        // Verify gender coverage
        val femaleVoices = curatedVoices.filter { it.gender == "Female" }
        val maleVoices = curatedVoices.filter { it.gender == "Male" }
        assertTrue("Should provide female voices", femaleVoices.isNotEmpty())
        assertTrue("Should provide male voices", maleVoices.isNotEmpty())

        // Verify regional accents coverage
        val accents = curatedVoices.map { it.accent }.toSet()
        assertTrue("Should include American accent", accents.contains("American"))
        assertTrue("Should include British accent", accents.contains("British"))
        assertTrue("Should include Indian accent", accents.contains("Indian"))
        assertTrue("Should include Australian accent", accents.contains("Australian"))

        // Verify multilingual regional voices
        val languages = curatedVoices.map { it.languageCode }.toSet()
        assertTrue("Should include English voices", languages.contains("en"))
        assertTrue("Should include Hindi voices", languages.contains("hi"))
        assertTrue("Should include Bengali voices", languages.contains("bn"))

        // Verify individual voice info metadata integrity
        val usFemale = curatedVoices.first { it.id == "en-us-natural-f" }
        assertEquals("Clara", usFemale.name)
        assertEquals("American", usFemale.accent)
        assertEquals("Female", usFemale.gender)
        assertEquals("🇺🇸", usFemale.flag)
        assertEquals("👩", usFemale.genderIcon)
        assertTrue(usFemale.samplePhrase.isNotBlank())
    }
}
