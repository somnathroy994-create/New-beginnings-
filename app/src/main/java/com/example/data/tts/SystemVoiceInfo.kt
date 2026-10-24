package com.example.data.tts

import java.util.Locale

data class SystemVoiceInfo(
    val id: String,
    val name: String,
    val displayName: String,
    val languageCode: String,
    val languageName: String,
    val flag: String,
    val locale: Locale,
    val gender: String, // "Female", "Male", "Neutral"
    val accent: String, // "American", "British", "Indian", "Australian", "Neutral"
    val quality: String = "High Quality", // "Neural", "High Quality", "Standard"
    val isNetworkRequired: Boolean = false,
    val samplePhrase: String = "Hello! This is how I read books, articles, and news aloud."
) {
    val genderIcon: String
        get() = when (gender.lowercase()) {
            "female" -> "👩"
            "male" -> "👨"
            else -> "🗣️"
        }
}
