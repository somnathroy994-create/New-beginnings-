package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String,
    val articleId: String? = null,
    val articleTitle: String? = null,
    val lineIndex: Int? = null,
    val quote: String? = null,
    val noteText: String,
    val tag: String = "Insight", // Insight, Quote, Question, Summary, Vocabulary
    val colorHex: String = "#38BDF8", // Accent color tag
    val createdAt: Long = System.currentTimeMillis()
)
