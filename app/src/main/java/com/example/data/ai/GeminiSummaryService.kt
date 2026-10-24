package com.example.data.ai

import android.util.Log
import com.example.data.model.Article
import com.example.data.model.SummaryConfig
import com.example.data.model.SummaryResult
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiSummaryService"

object GeminiSummaryService {

    // Define strict JSON schema for structured summary output
    private val summarySchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Concise title or heading for the summary"),
            "executiveSummary" to Schema.string(description = "Executive summary text according to requested length and tone"),
            "keyTakeaways" to Schema.array(
                items = Schema.string(),
                description = "List of key takeaways or bullet points"
            ),
            "mainTopics" to Schema.array(
                items = Schema.string(),
                description = "3 to 6 key subject tags or thematic labels"
            ),
            "keyConcepts" to Schema.array(
                items = Schema.string(),
                description = "Core concepts or key vocabulary terms explained"
            ),
            "difficultyLevel" to Schema.string(description = "Accessibility level: Accessible, Intermediate, or Advanced"),
            "tone" to Schema.string(description = "Descriptor of the tone utilized in the summary")
        ),
        optionalProperties = listOf("keyConcepts")
    )

    private val genConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = summarySchema
    }

    // Lazy initialization of Gemini 3.8 Flash model via Firebase AI Logic
    private val geminiModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = genConfig
        )
    }

    suspend fun generateSummary(
        article: Article,
        config: SummaryConfig = SummaryConfig()
    ): SummaryResult {
        val words = article.content.split(Regex("\\s+")).filter { it.isNotBlank() }
        val totalWords = words.size
        val totalSentences = article.getLines().size
        val estMinutes = (totalWords / 180).coerceAtLeast(1)

        val targetLangInstruction = if (config.targetLanguage.equals("Original", ignoreCase = true)) {
            "Language: Write the summary in the original language of the material (${article.languageName})."
        } else {
            "Language: Write the summary in ${config.targetLanguage}."
        }

        val prompt = """
            You are an expert reading assistant and literary analyst.
            Summarize the following reading material using the requested configuration parameters:
            
            Article Title: ${article.title}
            Author / Source: ${article.author} (${article.source})
            Category: ${article.category}
            
            CONFIGURATION:
            - Format & Depth: ${config.length.label} (${config.length.promptDesc})
            - Tone: ${config.tone.label} (${config.tone.promptDesc})
            - Focus: ${config.focus.label} (${config.focus.promptDesc})
            - $targetLangInstruction
            
            CONTENT TO SUMMARIZE:
            ${article.content}
            
            Produce a structured response matching the JSON schema:
            - "title": A sharp title for this summary
            - "executiveSummary": The core summary text tailored to the requested format and tone
            - "keyTakeaways": 3-5 bulleted takeaway items
            - "mainTopics": 3-5 keywords or theme tags (without '#' symbol)
            - "keyConcepts": 2-4 important concepts or vocabulary terms from the text with brief definitions
            - "difficultyLevel": "Accessible", "Intermediate", or "Advanced"
            - "tone": Short phrase describing the tone applied
        """.trimIndent()

        try {
            // Mandated: realistic 30-60s timeout for model inference
            val response = withTimeout(60_000L) {
                geminiModel.generateContent(prompt)
            }

            val rawJson = response.text
            if (rawJson.isNullOrBlank()) {
                throw IllegalStateException("Gemini returned an empty response.")
            }

            val json = JSONObject(rawJson)
            val title = json.optString("title", article.title)
            val executiveSummary = json.optString("executiveSummary", "")

            val takeaways = mutableListOf<String>()
            val takeawaysArr = json.optJSONArray("keyTakeaways")
            if (takeawaysArr != null) {
                for (i in 0 until takeawaysArr.length()) {
                    takeaways.add(takeawaysArr.getString(i))
                }
            }

            val topics = mutableListOf<String>()
            val topicsArr = json.optJSONArray("mainTopics")
            if (topicsArr != null) {
                for (i in 0 until topicsArr.length()) {
                    topics.add(topicsArr.getString(i))
                }
            }

            val concepts = mutableListOf<String>()
            val conceptsArr = json.optJSONArray("keyConcepts")
            if (conceptsArr != null) {
                for (i in 0 until conceptsArr.length()) {
                    concepts.add(conceptsArr.getString(i))
                }
            }

            val diffLevel = json.optString("difficultyLevel", "Intermediate")
            val toneApplied = json.optString("tone", config.tone.label)

            return SummaryResult(
                title = title,
                executiveSummary = executiveSummary,
                keyTakeaways = if (takeaways.isNotEmpty()) takeaways else listOf("Key insight extracted from content."),
                mainTopics = topics,
                wordCount = totalWords,
                sentenceCount = totalSentences,
                estimatedReadMinutes = estMinutes,
                difficultyLevel = diffLevel,
                tone = toneApplied,
                keyConcepts = concepts,
                source = "Gemini 3.8 Flash"
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini summary generation timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini summary inference error: ${e.message}", e)
            throw e
        }
    }
}
