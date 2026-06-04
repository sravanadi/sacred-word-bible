package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        TranslationEntity::class,
        BookEntity::class,
        VerseEntity::class,
        BookmarkEntity::class,
        HighlightEntity::class,
        NoteEntity::class,
        PrayerEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SacredWordDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao
    abstract fun bookDao(): BookDao
    abstract fun verseDao(): VerseDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun highlightDao(): HighlightDao
    abstract fun noteDao(): NoteDao
    abstract fun prayerDao(): PrayerDao
}
