package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.VocabularyWord
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY createdAt DESC")
    fun getAllWords(): Flow<List<VocabularyWord>>

    @Query("SELECT * FROM vocabulary WHERE masteryStatus = :status ORDER BY createdAt DESC")
    fun getWordsByMastery(status: String): Flow<List<VocabularyWord>>

    @Query("SELECT * FROM vocabulary WHERE articleId = :articleId ORDER BY createdAt DESC")
    fun getWordsForArticle(articleId: String): Flow<List<VocabularyWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyWord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<VocabularyWord>)

    @Update
    suspend fun updateWord(word: VocabularyWord)

    @Query("UPDATE vocabulary SET masteryStatus = :status, reviewCount = reviewCount + 1 WHERE id = :id")
    suspend fun updateMastery(id: String, status: String)

    @Delete
    suspend fun deleteWord(word: VocabularyWord)

    @Query("DELETE FROM vocabulary WHERE id = :id")
    suspend fun deleteWordById(id: String)
}
