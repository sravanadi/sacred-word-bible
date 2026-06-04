package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey val id: String,      // e.g. "BSB"
    val shortName: String,
    val englishName: String,
    val localName: String,           // name in its own language
    val language: String,            // ISO 639 code e.g. "eng"
    val textDirection: String,       // "ltr" or "rtl"
    val isDownloaded: Boolean = false,
    val downloadedAt: Long? = null
)

@Entity(
    tableName = "books",
    indices = [
        Index(value = ["translationId", "bookId"])
    ]
)
data class BookEntity(
    @PrimaryKey val id: String,      // e.g. "BSB_GEN"
    val translationId: String,
    val bookId: String,              // e.g. "GEN"
    val name: String,
    val commonName: String,
    val bookOrder: Int,
    val totalChapters: Int
)

@Entity(
    tableName = "verses",
    indices = [
        Index(value = ["translationId"]),
        Index(value = ["translationId", "bookId", "chapterNumber"])
    ]
)
data class VerseEntity(
    @PrimaryKey val id: String,      // e.g. "BSB_GEN_1_1"
    val translationId: String,
    val bookId: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val text: String
)

@Entity(
    tableName = "bookmarks",
    indices = [
        Index(value = ["verseId"])
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val verseId: String,             // FK to VerseEntity.id
    val translationId: String,
    val bookId: String,
    val bookName: String,
    val chapterNumber: Int,
    val verseNumber: Int,
    val verseText: String,           // cached for fast display
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey val verseId: String, // FK to VerseEntity.id
    val colorHex: String,            // e.g. "#7B5EA7"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prayers")
data class PrayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isAnswered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

