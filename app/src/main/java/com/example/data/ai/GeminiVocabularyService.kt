package com.example.data.ai

import android.util.Log
import com.example.data.model.Article
import com.example.data.model.VocabularyWord
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.util.UUID

private const val TAG = "GeminiVocabService"

data class WordEnrichment(
    val word: String,
    val phonetic: String,
    val partOfSpeech: String,
    val definition: String,
    val translation: String,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val exampleSentence: String
)

object GeminiVocabularyService {

    private val vocabSchema = Schema.obj(
        properties = mapOf(
            "word" to Schema.string(description = "The target headword in base form"),
            "phonetic" to Schema.string(description = "IPA phonetic pronunciation guide, e.g. /ˌiːkwɪˈlɪbriəm/"),
            "partOfSpeech" to Schema.string(description = "Grammatical part of speech: Noun, Verb, Adjective, Adverb, Idiom"),
            "definition" to Schema.string(description = "Clear, accurate definition in the requested context"),
            "translation" to Schema.string(description = "Meaning / translation in Bengali, Hindi, or English as appropriate"),
            "synonyms" to Schema.array(
                items = Schema.string(),
                description = "3 to 6 common synonyms for this word in this context"
            ),
            "antonyms" to Schema.array(
                items = Schema.string(),
                description = "2 to 5 direct antonyms or opposite terms for this word"
            ),
            "exampleSentence" to Schema.string(description = "Natural example sentence demonstrating the word's usage")
        ),
        optionalProperties = listOf("antonyms")
    )

    private val genConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = vocabSchema
    }

    private val vocabModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = genConfig
        )
    }

    suspend fun enrichWordWithAi(
        word: String,
        contextSentence: String,
        article: Article?
    ): VocabularyWord {
        val languageCode = article?.languageCode ?: "en"
        val languageName = article?.languageName ?: "English"

        val prompt = """
            You are a lexicographer and language tutor.
            Analyze the following word in its reading context:
            
            Target Word: "$word"
            Sentence Context: "$contextSentence"
            Source Language: $languageName ($languageCode)
            
            Provide comprehensive lexical analysis:
            1. IPA phonetic transcription.
            2. Part of speech.
            3. Contextual definition.
            4. Translation or equivalent in English (if non-English) or in Bengali/Hindi (if English).
            5. 3 to 6 rich SYNONYMS.
            6. 2 to 5 distinct ANTONYMS (if applicable, else empty list).
            7. Example sentence.
            
            Return raw JSON according to schema.
        """.trimIndent()

        try {
            val response = withTimeout(30_000L) {
                vocabModel.generateContent(prompt)
            }

            val rawJson = response.text
            if (rawJson.isNullOrBlank()) {
                throw IllegalStateException("Gemini returned empty text for vocabulary lookup.")
            }

            val json = JSONObject(rawJson)
            val headword = json.optString("word", word)
            val phonetic = json.optString("phonetic", "/${word.lowercase()}/")
            val partOfSpeech = json.optString("partOfSpeech", "Noun")
            val definition = json.optString("definition", "")
            val translation = json.optString("translation", "")

            val synonymsList = mutableListOf<String>()
            val synArr = json.optJSONArray("synonyms")
            if (synArr != null) {
                for (i in 0 until synArr.length()) {
                    synonymsList.add(synArr.getString(i))
                }
            }

            val antonymsList = mutableListOf<String>()
            val antArr = json.optJSONArray("antonyms")
            if (antArr != null) {
                for (i in 0 until antArr.length()) {
                    antonymsList.add(antArr.getString(i))
                }
            }

            val exampleSentence = json.optString("exampleSentence", contextSentence)

            return VocabularyWord(
                id = UUID.randomUUID().toString(),
                word = headword,
                languageCode = languageCode,
                phonetic = phonetic,
                partOfSpeech = partOfSpeech,
                definition = definition.ifBlank { "Definition of $word in context." },
                exampleSentence = exampleSentence,
                translation = translation,
                targetLanguage = "English",
                masteryStatus = "Learning",
                articleId = article?.id,
                articleTitle = article?.title,
                synonyms = synonymsList.joinToString(", "),
                antonyms = antonymsList.joinToString(", ")
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini vocabulary lookup timed out", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini vocabulary lookup failed: ${e.message}", e)
            throw e
        }
    }
}
