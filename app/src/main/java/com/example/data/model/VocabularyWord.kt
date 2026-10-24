package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class VocabularyWord(
    @PrimaryKey
    val id: String,
    val word: String,
    val languageCode: String = "en",
    val phonetic: String = "",
    val partOfSpeech: String = "Noun",
    val definition: String,
    val exampleSentence: String = "",
    val translation: String = "",
    val targetLanguage: String = "English",
    val masteryStatus: String = "Learning", // Learning, Reviewing, Mastered
    val articleId: String? = null,
    val articleTitle: String? = null,
    val reviewCount: Int = 0,
    val synonyms: String = "", // Comma-separated or JSON string
    val antonyms: String = "", // Comma-separated or JSON string
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getSynonymList(): List<String> = if (synonyms.isBlank()) emptyList() else synonyms.split(",").map { it.trim() }.filter { it.isNotBlank() }
    fun getAntonymList(): List<String> = if (antonyms.isBlank()) emptyList() else antonyms.split(",").map { it.trim() }.filter { it.isNotBlank() }
}
