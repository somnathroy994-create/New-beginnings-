package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String = "Various",
    val source: String = "General",
    val category: String = "News", // News, Book, Literature, Science, Language Learning
    val languageCode: String = "en", // en, es, fr, de, hi, ja, etc.
    val languageName: String = "English",
    val content: String,
    val lastReadLineIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
) {
    /**
     * Splits content into clean sentence-based lines for line-by-line reading aloud.
     */
    fun getLines(): List<String> {
        if (content.isBlank()) return emptyList()
        // Split by sentence terminators or line breaks, preserving clean sentences
        val rawLines = content.split(Regex("(?<=[.!?।。！？])\\s+|\\n+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        return if (rawLines.isEmpty()) listOf(content.trim()) else rawLines
    }
}
