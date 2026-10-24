package com.example.data.model

data class SummaryResult(
    val title: String,
    val executiveSummary: String,
    val keyTakeaways: List<String>,
    val mainTopics: List<String>,
    val wordCount: Int,
    val sentenceCount: Int,
    val estimatedReadMinutes: Int,
    val difficultyLevel: String, // Beginner, Intermediate, Advanced
    val tone: String,
    val keyConcepts: List<String> = emptyList(),
    val source: String = "Gemini AI"
)
