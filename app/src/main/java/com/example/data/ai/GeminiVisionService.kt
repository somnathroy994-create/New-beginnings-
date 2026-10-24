package com.example.data.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiVisionService"

data class ExtractedScanResult(
    val title: String,
    val author: String,
    val source: String,
    val category: String,
    val languageCode: String,
    val languageName: String,
    val content: String,
    val detectedType: String = "Book / Newspaper Scan"
)

object GeminiVisionService {

    // Define JSON schema for structured book page & newspaper OCR extraction
    private val scanSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Headline or chapter title of the scanned book page or newspaper"),
            "author" to Schema.string(description = "Author, journalist, or publication byline if discernible; otherwise 'Scanned Material'"),
            "source" to Schema.string(description = "Name of the newspaper, book, or publisher if visible; otherwise 'Print Media Scan'"),
            "category" to Schema.string(description = "Category: News, History, Science, Literature, Editorial, or Lifestyle"),
            "languageCode" to Schema.string(description = "ISO language code, e.g., en, hi, bn, ta, zh, es, fr, de"),
            "languageName" to Schema.string(description = "Full human-readable language name, e.g., English, Hindi, Bengali, Tamil, etc."),
            "detectedType" to Schema.string(description = "'Newspaper Article', 'Book Page', 'Editorial Column', or 'Document'"),
            "content" to Schema.string(description = "The complete, faithful extracted text of the page. Preserve paragraph breaks with double newlines (\\n\\n) and standard punctuation suitable for sentence-by-sentence read aloud.")
        ),
        optionalProperties = listOf("author", "source", "detectedType")
    )

    private val genConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = scanSchema
    }

    private val visionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = genConfig
        )
    }

    suspend fun extractTextFromPageImage(bitmap: Bitmap): ExtractedScanResult {
        // Downscale bitmap if exceptionally large to ensure under 20MB payload limit
        val scaledBitmap = if (bitmap.width > 2048 || bitmap.height > 2048) {
            val ratio = Math.min(2048f / bitmap.width, 2048f / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * ratio).toInt(),
                (bitmap.height * ratio).toInt(),
                true
            )
        } else {
            bitmap
        }

        val prompt = """
            You are an advanced Optical Character Recognition (OCR) and document intelligence specialist.
            Examine this scanned image of a book page, newspaper article, magazine, or print material.
            
            Perform accurate, complete OCR extraction:
            1. Transcribe ALL visible text accurately, faithfully preserving punctuation and paragraph order.
            2. Identify the main headline or chapter title.
            3. Detect the author, reporter, or source if printed on the page.
            4. Detect the primary language (English, Hindi, Bengali, Tamil, Chinese, Spanish, French, etc.) and ISO code.
            5. Determine if it is a 'Newspaper Article', 'Book Page', or 'Document'.
            6. Format the 'content' field cleanly with proper sentence capitalization and newline spacing between paragraphs so a Text-to-Speech (TTS) reader can read it aloud fluently.
            
            Output strictly in JSON matching the specified schema.
        """.trimIndent()

        try {
            val response = withTimeout(60_000L) {
                visionModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(prompt)
                    }
                )
            }

            val rawJson = response.text
            if (rawJson.isNullOrBlank()) {
                throw IllegalStateException("Gemini returned empty text for scanned page.")
            }

            val json = JSONObject(rawJson)
            val title = json.optString("title", "Scanned Print Page").ifBlank { "Scanned Print Page" }
            val author = json.optString("author", "Print Media").ifBlank { "Print Media" }
            val source = json.optString("source", "Camera / Gallery Scan").ifBlank { "Camera / Gallery Scan" }
            val category = json.optString("category", "General").ifBlank { "General" }
            val languageCode = json.optString("languageCode", "en").ifBlank { "en" }
            val languageName = json.optString("languageName", "English").ifBlank { "English" }
            val detectedType = json.optString("detectedType", "Book / Newspaper Scan")
            val content = json.optString("content", "")

            if (content.isBlank()) {
                throw IllegalStateException("No legible text could be extracted from the scanned image.")
            }

            return ExtractedScanResult(
                title = title,
                author = author,
                source = source,
                category = category,
                languageCode = languageCode,
                languageName = languageName,
                content = content,
                detectedType = detectedType
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini vision scan timed out after 60s", e)
            throw e
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini vision scan error: ${e.message}", e)
            throw e
        }
    }

    // Demo / Preset Scanned Samples for quick verification or testing on emulator without a physical newspaper
    val sampleScans: List<ExtractedScanResult> = listOf(
        ExtractedScanResult(
            title = "Morning Chronicle: The Clean Energy Grid Revolution",
            author = "Sarah Jenkins, Energy Correspondent",
            source = "The Daily Chronicle (Front Page)",
            category = "News",
            languageCode = "en",
            languageName = "English",
            detectedType = "Newspaper Article",
            content = """Next-generation renewable microgrids reached a historic milestone yesterday, powering over forty percent of regional commercial hubs without fossil backup.
Engineers in California and Texas deployed high-capacity iron-air batteries capable of discharging stored solar power for over one hundred hours continuously.
Municipal leaders praised the breakthrough, noting that community power stability survived record heatwaves without rolling blackouts.
Grid architects emphasize that decentralized storage and smart inverters transform vulnerable electrical networks into self-healing resilient power ecosystems.
Federal regulators announced expedited permitting for similar green infrastructure deployments nationwide throughout the upcoming fiscal quarter."""
        ),
        ExtractedScanResult(
            title = "दैनिक जागरण: चंद्रयान और भारत की अंतरिक्ष यात्रा",
            author = "विशेष संवाददाता (Special Correspondent)",
            source = "दैनिक राष्ट्रीय समाचार (Newspaper Editorial)",
            category = "News",
            languageCode = "hi",
            languageName = "Hindi (हिन्दी)",
            detectedType = "Newspaper Article",
            content = """भारतीय अंतरिक्ष अनुसंधान संगठन (इसरो) ने चंद्र सतह पर वैज्ञानिक परीक्षणों का नया चरण सफलतापूर्वक पूरा किया।
रोवर द्वारा भेजे गए रासायनिक आंकड़ों से सतह पर सल्फर, एल्यूमीनियम और टाइटेनियम की प्रचुर उपस्थिति की पुष्टि हुई है।
वैज्ञानिकों का मानना है कि चंद्रमा के दक्षिणी ध्रुव पर स्थित जल-बर्फ के भंडार भविष्य के मानव अभियानों के लिए अत्यंत महत्वपूर्ण सिद्ध होंगे।
इस ऐतिहासिक उपलब्धि ने वैश्विक अंतरिक्ष विज्ञान में भारत की तकनीकी क्षमता और आत्मनिर्भरता का नया मानदंड स्थापित किया है।
देश भर के विश्वविद्यालयों और युवा शोधकर्ताओं में इस मिशन को लेकर अपार उत्साह देखा जा रहा है।"""
        ),
        ExtractedScanResult(
            title = "The Secret Garden: Chapter Four (Springtime Awakens)",
            author = "Frances Hodgson Burnett",
            source = "Classic Literature Book Page",
            category = "Literature",
            languageCode = "en",
            languageName = "English",
            detectedType = "Book Page",
            content = """Mary Lennox stepped softly through the ivy-covered arched doorway, holding her breath in wonder at the quiet world inside.
A warm breeze rustled the dormant rose branches, carrying the faint, earthy fragrance of damp soil and awakening roots.
Tiny pale green shoots were thrusting their way upward through the thick winter leaves toward the pale morning sunshine.
She knelt on the mossy flagstones, gently clearing away the dead grass to give the little sprouts room to breathe.
For the first time since arriving at Misselthwaite Manor, a genuine feeling of happiness and curiosity warmed her lonely heart."""
        ),
        ExtractedScanResult(
            title = "আনন্দবাজার পত্রিকা: সুন্দরবনের ম্যানগ্রোভ সংরক্ষণ অভিযান",
            author = "পরিবেশ ডেস্ক",
            source = "দৈনিক সংবাদপত্র (Daily Newspaper)",
            category = "News",
            languageCode = "bn",
            languageName = "Bengali (বাংলা)",
            detectedType = "Newspaper Article",
            content = """সুন্দরবনের উপকূলীয় ম্যানগ্রোভ বন রক্ষায় স্থানীয় গ্রামবাসী ও বনদপ্তরের যৌথ উদ্যোগে শুরু হল বিশেষ বৃক্ষরোপণ কর্মসূচি।
ঘূর্ণিঝড়ের তীব্রতা প্রতিহত করতে গোলপাতা, সুন্দরী ও গরান গাছের চারা রোপণ করা হচ্ছে নদী তীরবর্তী বাঁধগুলোতে।
বিশেষজ্ঞরা জানিয়েছেন, এই বাদাবন সমগ্র দক্ষিণবঙ্গের প্রাকৃতিক ঢাল হিসেবে কাজ করে এবং সামুদ্রিক জীববৈচিত্র্যের প্রধান আধার।
স্থানীয় মহিলাদের স্বনির্ভর দলগুলো চারাগাছ পরিচর্যা ও পাহারার দায়িত্ব নিয়ে নতুন নজির গড়েছে।
পরিবেশবিদদের মতে, প্রকৃতির এই রক্ষাকবচ বাঁচিয়ে রাখাই ভবিষ্যৎ প্রজন্মের জলবায়ু সুরক্ষার একমাত্র চাবিকাঠি।"""
        )
    )
}
