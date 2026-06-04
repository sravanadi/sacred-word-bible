package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations ORDER BY language ASC, englishName ASC")
    fun getAllTranslations(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translations WHERE isDownloaded = 1 ORDER BY englishName ASC")
    fun getDownloadedTranslations(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translations WHERE id = :id LIMIT 1")
    suspend fun getTranslationById(id: String): TranslationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslations(translations: List<TranslationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationEntity)

    @Update
    suspend fun updateTranslation(translation: TranslationEntity)

    @Query("DELETE FROM translations WHERE id = :id")
    suspend fun deleteTranslation(id: String)
}

@Dao
interface BookDao {
    @Query("SELECT * FROM books WHERE translationId = :translationId ORDER BY bookOrder ASC")
    fun getBooksByTranslation(translationId: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE translationId = :translationId ORDER BY bookOrder ASC")
    suspend fun getBooksByTranslationSync(translationId: String): List<BookEntity>

    @Query("SELECT * FROM books WHERE translationId = :translationId AND bookId = :bookId LIMIT 1")
    suspend fun getBook(translationId: String, bookId: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookEntity>)

    @Query("DELETE FROM books WHERE translationId = :translationId")
    suspend fun deleteBooksByTranslation(translationId: String)
}

@Dao
interface VerseDao {
    @Query("SELECT * FROM verses WHERE translationId = :translationId AND bookId = :bookId AND chapterNumber = :chapterNumber ORDER BY verseNumber ASC")
    fun getVerses(translationId: String, bookId: String, chapterNumber: Int): Flow<List<VerseEntity>>

    @Query("SELECT * FROM verses WHERE translationId = :translationId AND bookId = :bookId AND chapterNumber = :chapterNumber ORDER BY verseNumber ASC")
    suspend fun getVersesSync(translationId: String, bookId: String, chapterNumber: Int): List<VerseEntity>

    @Query("SELECT * FROM verses WHERE id = :id LIMIT 1")
    suspend fun getVerseById(id: String): VerseEntity?

    @Query("SELECT COUNT(*) FROM verses WHERE translationId = :translationId")
    suspend fun getVersesCount(translationId: String): Int

    @Query("SELECT * FROM verses WHERE translationId = :translationId LIMIT 1 OFFSET :offset")
    suspend fun getVerseAtOffset(translationId: String, offset: Int): VerseEntity?

    @Query("SELECT * FROM verses WHERE translationId = :translationId ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomVerse(translationId: String): VerseEntity?

    @Query("SELECT * FROM verses LIMIT 1")
    suspend fun getAnyVerse(): VerseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerses(verses: List<VerseEntity>)

    @Query("DELETE FROM verses WHERE translationId = :translationId")
    suspend fun deleteVersesByTranslation(translationId: String)
}

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE verseId = :verseId LIMIT 1")
    suspend fun getBookmarkByVerseId(verseId: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE verseId = :verseId")
    suspend fun deleteBookmarkByVerseId(verseId: String)
}

@Dao
interface HighlightDao {
    @Query("SELECT * FROM highlights")
    fun getAllHighlights(): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE verseId = :verseId LIMIT 1")
    suspend fun getHighlightByVerseId(verseId: String): HighlightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE verseId = :verseId")
    suspend fun deleteHighlightByVerse(verseId: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayers ORDER BY createdAt DESC")
    fun getAllPrayers(): Flow<List<PrayerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayer: PrayerEntity)

    @Query("DELETE FROM prayers WHERE id = :id")
    suspend fun deletePrayerById(id: Long)

    @Query("UPDATE prayers SET isAnswered = :isAnswered WHERE id = :id")
    suspend fun updatePrayerStatus(id: Long, isAnswered: Boolean)
}

