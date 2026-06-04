package com.example.data.repository

import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.data.remote.BibleApiService
import com.example.data.remote.ScriptureApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class BibleRepository(
    private val apiService: BibleApiService,
    private val scriptureApiService: ScriptureApiService,
    private val translationDao: TranslationDao,
    private val bookDao: BookDao,
    private val verseDao: VerseDao,
    private val bookmarkDao: BookmarkDao,
    private val highlightDao: HighlightDao,
    private val noteDao: NoteDao,
    private val prayerDao: PrayerDao
) {

    val allTranslations: Flow<List<TranslationEntity>> = translationDao.getAllTranslations()
    val downloadedTranslations: Flow<List<TranslationEntity>> = translationDao.getDownloadedTranslations()
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val allHighlights: Flow<List<HighlightEntity>> = highlightDao.getAllHighlights()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allPrayers: Flow<List<PrayerEntity>> = prayerDao.getAllPrayers()

    suspend fun insertNote(title: String, content: String) {
        withContext(Dispatchers.IO) {
            noteDao.insertNote(NoteEntity(title = title, content = content))
        }
    }

    suspend fun deleteNote(id: Long) {
        withContext(Dispatchers.IO) {
            noteDao.deleteNoteById(id)
        }
    }

    suspend fun insertPrayer(title: String, content: String) {
        withContext(Dispatchers.IO) {
            prayerDao.insertPrayer(PrayerEntity(title = title, content = content))
        }
    }

    suspend fun deletePrayer(id: Long) {
        withContext(Dispatchers.IO) {
            prayerDao.deletePrayerById(id)
        }
    }

    suspend fun updatePrayerStatus(id: Long, isAnswered: Boolean) {
        withContext(Dispatchers.IO) {
            prayerDao.updatePrayerStatus(id, isAnswered)
        }
    }

    suspend fun fetchAvailableTranslations() {
        withContext(Dispatchers.IO) {
            try {
                // Fetch default helloao offline translations
                val apiResponse = apiService.getAvailableTranslations()
                val entities = apiResponse.translations.map { apiTrans ->
                    val existing = translationDao.getTranslationById(apiTrans.id)
                    TranslationEntity(
                        id = apiTrans.id,
                        shortName = apiTrans.shortName ?: apiTrans.id,
                        englishName = apiTrans.englishName ?: apiTrans.id,
                        localName = apiTrans.localName ?: apiTrans.englishName ?: apiTrans.id,
                        language = apiTrans.language ?: "",
                        textDirection = apiTrans.textDirection ?: "ltr",
                        isDownloaded = existing?.isDownloaded ?: false,
                        downloadedAt = existing?.downloadedAt
                    )
                }
                translationDao.insertTranslations(entities)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    val fallbackTranslations = listOf(
                        TranslationEntity(
                            id = "eng_kjv",
                            shortName = "KJV",
                            englishName = "King James Version",
                            localName = "King James Version",
                            language = "eng",
                            textDirection = "ltr",
                            isDownloaded = translationDao.getTranslationById("eng_kjv")?.isDownloaded ?: false,
                            downloadedAt = translationDao.getTranslationById("eng_kjv")?.downloadedAt
                        ),
                        TranslationEntity(
                            id = "eng_web",
                            shortName = "WEBC",
                            englishName = "World English Bible Classic",
                            localName = "World English Bible Classic",
                            language = "eng",
                            textDirection = "ltr",
                            isDownloaded = translationDao.getTranslationById("eng_web")?.isDownloaded ?: false,
                            downloadedAt = translationDao.getTranslationById("eng_web")?.downloadedAt
                        ),
                        TranslationEntity(
                            id = "por_blj",
                            shortName = "BLJ",
                            englishName = "Portuguese Bíblia Livre",
                            localName = "Portuguese Bíblia Livre",
                            language = "por",
                            textDirection = "ltr",
                            isDownloaded = translationDao.getTranslationById("por_blj")?.isDownloaded ?: false,
                            downloadedAt = translationDao.getTranslationById("por_blj")?.downloadedAt
                        ),
                        TranslationEntity(
                            id = "spa_r09",
                            shortName = "R09",
                            englishName = "Reina Valera 1909",
                            localName = "Reina Valera 1909",
                            language = "spa",
                            textDirection = "ltr",
                            isDownloaded = translationDao.getTranslationById("spa_r09")?.isDownloaded ?: false,
                            downloadedAt = translationDao.getTranslationById("spa_r09")?.downloadedAt
                        )
                    )
                    translationDao.insertTranslations(fallbackTranslations)
                } catch (dbEx: Exception) {
                    dbEx.printStackTrace()
                }
            }

            try {
                // Delete placeholders if they were previously inserted to clean up the DB
                translationDao.deleteTranslation("9851ab6653436f15-01")
                translationDao.deleteTranslation("de4e12af7c8c68b8-01")
                translationDao.deleteTranslation("061262d1d293fa0a-01")

                // Fetch dynamic online translations if API key is set
                val apiKey = getBibleApiKey()
                if (apiKey.isNotBlank()) {
                    val scriptureResponse = scriptureApiService.getBibles(apiKey)
                    val scriptureEntities = scriptureResponse.data.map { b ->
                        val existing = translationDao.getTranslationById(b.id)
                        TranslationEntity(
                            id = b.id,
                            shortName = b.abbreviation ?: b.id,
                            englishName = b.name,
                            localName = b.nameLocal ?: b.name,
                            language = b.language?.id ?: "",
                            textDirection = if (b.language?.scriptDirection?.lowercase() == "rtl") "rtl" else "ltr",
                            isDownloaded = existing?.isDownloaded ?: false,
                            downloadedAt = existing?.downloadedAt
                        )
                    }
                    translationDao.insertTranslations(scriptureEntities)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getBooks(translationId: String): Flow<List<BookEntity>> {
        return bookDao.getBooksByTranslation(translationId)
    }

    suspend fun getBooksSync(translationId: String): List<BookEntity> {
        return withContext(Dispatchers.IO) {
            bookDao.getBooksByTranslationSync(translationId)
        }
    }

    suspend fun getBook(translationId: String, bookId: String): BookEntity? {
        return withContext(Dispatchers.IO) {
            bookDao.getBook(translationId, bookId)
        }
    }

    fun getVerses(translationId: String, bookId: String, chapterNumber: Int): Flow<List<VerseEntity>> {
        return verseDao.getVerses(translationId, bookId, chapterNumber)
    }

    suspend fun getVersesSync(translationId: String, bookId: String, chapterNumber: Int): List<VerseEntity> {
        return withContext(Dispatchers.IO) {
            verseDao.getVersesSync(translationId, bookId, chapterNumber)
        }
    }

    suspend fun getVerseById(id: String): VerseEntity? {
        return withContext(Dispatchers.IO) {
            verseDao.getVerseById(id)
        }
    }

    suspend fun getVersesCount(translationId: String): Int {
        return withContext(Dispatchers.IO) {
            verseDao.getVersesCount(translationId)
        }
    }

    suspend fun getVerseAtOffset(translationId: String, offset: Int): VerseEntity? {
        return withContext(Dispatchers.IO) {
            verseDao.getVerseAtOffset(translationId, offset)
        }
    }

    suspend fun getRandomVerse(translationId: String): VerseEntity? {
        return withContext(Dispatchers.IO) {
            verseDao.getRandomVerse(translationId)
        }
    }

    suspend fun getAnyVerse(): VerseEntity? {
        return withContext(Dispatchers.IO) {
            verseDao.getAnyVerse()
        }
    }

    suspend fun downloadTranslation(translationId: String, onProgress: (Float) -> Unit) {
        if (isApiBibleId(translationId)) {
            withContext(Dispatchers.IO) {
                val apiKey = getBibleApiKey()
                if (apiKey.isBlank()) {
                    throw Exception("BIBLE_API_KEY is not set! Please click the Secrets icon on the left (key icon) and add a key named BIBLE_API_KEY in the Secrets panel.")
                }
                
                onProgress(0.1f)
                val booksResponse = scriptureApiService.getBooks(apiKey, translationId)
                val apiBooks = booksResponse.data
                if (apiBooks.isEmpty()) throw Exception("No books found for translation $translationId from API.Bible")
                
                val totalBooks = apiBooks.size
                val existingTranslation = translationDao.getTranslationById(translationId)
                val shortName = existingTranslation?.shortName ?: translationId
                val englishName = existingTranslation?.englishName ?: translationId
                val localName = existingTranslation?.localName ?: translationId
                val language = existingTranslation?.language ?: ""
                val textDirection = existingTranslation?.textDirection ?: "ltr"

                val updatedTranslation = TranslationEntity(
                    id = translationId,
                    shortName = shortName,
                    englishName = englishName,
                    localName = localName,
                    language = language,
                    textDirection = textDirection,
                    isDownloaded = false
                )
                translationDao.insertTranslation(updatedTranslation)

                bookDao.deleteBooksByTranslation(translationId)
                verseDao.deleteVersesByTranslation(translationId)

                val bookEntities = apiBooks.mapIndexed { index, b ->
                    val bookEntityId = "${translationId}_${b.id}"
                    val totalChapters = getStandardChapterCount(b.id)
                    BookEntity(
                        id = bookEntityId,
                        translationId = translationId,
                        bookId = b.id,
                        name = b.name,
                        commonName = b.nameLocal ?: b.name,
                        bookOrder = index + 1,
                        totalChapters = totalChapters
                    )
                }

                bookDao.insertBooks(bookEntities)
                onProgress(0.8f)

                translationDao.insertTranslation(
                    updatedTranslation.copy(
                        isDownloaded = true,
                        downloadedAt = System.currentTimeMillis()
                    )
                )
                onProgress(1.0f)
            }
            return
        }

        withContext(Dispatchers.IO) {
            onProgress(0.1f)
            val response = apiService.getBooks(translationId)
            val books = response.books ?: emptyList()
            if (books.isEmpty()) throw Exception("No books found for translation $translationId")
            
            val totalBooks = books.size
            val existingTranslation = translationDao.getTranslationById(translationId)
            val apiTranslation = response.translation
            val shortName = apiTranslation?.shortName ?: existingTranslation?.shortName ?: translationId
            val englishName = apiTranslation?.englishName ?: existingTranslation?.englishName ?: translationId
            val localName = apiTranslation?.localName ?: existingTranslation?.localName ?: translationId
            val language = apiTranslation?.language ?: existingTranslation?.language ?: ""
            val textDirection = apiTranslation?.textDirection ?: existingTranslation?.textDirection ?: "ltr"

            val updatedTranslation = TranslationEntity(
                id = translationId,
                shortName = shortName,
                englishName = englishName,
                localName = localName,
                language = language,
                textDirection = textDirection,
                isDownloaded = false
            )
            translationDao.insertTranslation(updatedTranslation)

            // Dynamic purging of partial downloaded state
            bookDao.deleteBooksByTranslation(translationId)
            verseDao.deleteVersesByTranslation(translationId)

            val bookEntities = books.mapIndexed { index, apiBook ->
                val bookEntityId = "${translationId}_${apiBook.id}"
                val totalChapters = apiBook.numberOfChapters ?: apiBook.chapters?.size ?: getStandardChapterCount(apiBook.id)
                BookEntity(
                    id = bookEntityId,
                    translationId = translationId,
                    bookId = apiBook.id,
                    name = apiBook.name,
                    commonName = apiBook.commonName ?: apiBook.name,
                    bookOrder = apiBook.order ?: apiBook.bookOrder ?: (index + 1),
                    totalChapters = totalChapters
                )
            }

            // Write books structure
            bookDao.insertBooks(bookEntities)
            onProgress(0.8f)

            // Flag download completion
            translationDao.insertTranslation(
                updatedTranslation.copy(
                    isDownloaded = true,
                    downloadedAt = System.currentTimeMillis()
                )
            )
            onProgress(1.0f)
        }
    }

    suspend fun deleteTranslation(translationId: String) {
        withContext(Dispatchers.IO) {
            val existing = translationDao.getTranslationById(translationId)
            if (existing != null) {
                bookDao.deleteBooksByTranslation(translationId)
                verseDao.deleteVersesByTranslation(translationId)
                translationDao.insertTranslation(
                    existing.copy(
                        isDownloaded = false,
                        downloadedAt = null
                    )
                )
            }
        }
    }

    suspend fun bookmarkVerse(verse: VerseEntity, bookName: String) {
        withContext(Dispatchers.IO) {
            val existing = bookmarkDao.getBookmarkByVerseId(verse.id)
            if (existing == null) {
                bookmarkDao.insertBookmark(
                    BookmarkEntity(
                        verseId = verse.id,
                        translationId = verse.translationId,
                        bookId = verse.bookId,
                        bookName = bookName,
                        chapterNumber = verse.chapterNumber,
                        verseNumber = verse.verseNumber,
                        verseText = verse.text
                    )
                )
            }
        }
    }

    suspend fun removeBookmark(verseId: String) {
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmarkByVerseId(verseId)
        }
    }

    suspend fun isBookmarked(verseId: String): Boolean {
        return withContext(Dispatchers.IO) {
            bookmarkDao.getBookmarkByVerseId(verseId) != null
        }
    }

    suspend fun setHighlight(verseId: String, colorHex: String) {
        withContext(Dispatchers.IO) {
            highlightDao.insertHighlight(
                HighlightEntity(
                    verseId = verseId,
                    colorHex = colorHex
                )
            )
        }
    }

    suspend fun removeHighlight(verseId: String) {
        withContext(Dispatchers.IO) {
            highlightDao.deleteHighlightByVerse(verseId)
        }
    }

    // --- Scripture.API.Bible Helper Integration methods ---

    fun isApiBibleId(id: String): Boolean {
        return id.contains("-") && id.length > 8
    }

    private fun getBibleApiKey(): String {
        return try {
            val key = com.example.BuildConfig.BIBLE_API_KEY
            if (key.isNotBlank() && !key.startsWith("YOUR_") && !key.startsWith("MY_")) {
                key
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun ensureChapterCached(translationId: String, bookId: String, chapterNumber: Int) {
        withContext(Dispatchers.IO) {
            val existing = verseDao.getVersesSync(translationId, bookId, chapterNumber)
            if (existing.isEmpty()) {
                if (isApiBibleId(translationId)) {
                    val apiKey = getBibleApiKey()
                    if (apiKey.isBlank()) return@withContext
                    try {
                        // API.Bible chapter matches pattern: BOOK.CHAPTER (e.g. GEN.1)
                        val chapterId = "${bookId}.${chapterNumber}"
                        val response = scriptureApiService.getChapterContent(
                            apiKey = apiKey,
                            bibleId = translationId,
                            chapterId = chapterId
                        )
                        val htmlContent = response.data.content ?: ""
                        val parsed = parseChapterHtml(htmlContent, bookId, chapterNumber)
                        val verseEntities = parsed.map { (vNum, text) ->
                            VerseEntity(
                                id = "${translationId}_${bookId}_${chapterNumber}_${vNum}",
                                translationId = translationId,
                                bookId = bookId,
                                chapterNumber = chapterNumber,
                                verseNumber = vNum,
                                text = text
                            )
                        }
                        if (verseEntities.isNotEmpty()) {
                            verseDao.insertVerses(verseEntities)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    // HelloAo API chapter
                    try {
                        val response = apiService.getChapter(translationId, bookId, chapterNumber)
                        val firstChapter = response.chapter
                        val verseEntities = mutableListOf<VerseEntity>()
                        firstChapter?.content?.forEach { item ->
                            if (item.type == "verse") {
                                val vNum = item.number ?: return@forEach
                                val textParts = item.content?.mapNotNull { part ->
                                    when (part) {
                                        is String -> part
                                        is Map<*, *> -> part["text"] as? String
                                        else -> null
                                    }
                                }?.joinToString("") ?: item.text ?: ""
                                if (textParts.isNotBlank()) {
                                    verseEntities.add(
                                        VerseEntity(
                                            id = "${translationId}_${bookId}_${chapterNumber}_${vNum}",
                                            translationId = translationId,
                                            bookId = bookId,
                                            chapterNumber = chapterNumber,
                                            verseNumber = vNum,
                                            text = textParts
                                        )
                                    )
                                }
                            }
                        }
                        if (verseEntities.isNotEmpty()) {
                            verseDao.insertVerses(verseEntities)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun parseChapterHtml(html: String, bookId: String, chapterNum: Int): List<Pair<Int, String>> {
        val result = mutableListOf<Pair<Int, String>>()
        
        // Match Span elements containing verse markers (class "v", "v-num" or id with the book prefix)
        val pattern = Regex("""<span[^>]*?(?:class=["'](?:v|v-num)[^"']*["']|id=["'][^"']*?\.\d+\.(\d+)["'])[^>]*?>(.*?)</span>""", RegexOption.IGNORE_CASE)
        val matches = pattern.findAll(html).toList()
        
        if (matches.isEmpty()) {
            val plainText = stripHtml(html)
            if (plainText.isNotBlank()) {
                result.add(1 to plainText)
            }
            return result
        }
        
        for (i in matches.indices) {
            val currentMatch = matches[i]
            var verseNum = currentMatch.groups[1]?.value?.toIntOrNull()
            if (verseNum == null) {
                val innerText = currentMatch.groups[2]?.value ?: ""
                verseNum = innerText.replace("&nbsp;", "").trim().toIntOrNull()
            }
            if (verseNum == null) {
                verseNum = i + 1
            }
            
            val startIdx = currentMatch.range.last + 1
            val endIdx = if (i < matches.size - 1) {
                matches[i + 1].range.first
            } else {
                html.length
            }
            
            val rawVerseText = html.substring(startIdx, endIdx)
            val cleanVerseText = stripHtml(rawVerseText)
            
            if (cleanVerseText.isNotBlank()) {
                result.add(verseNum to cleanVerseText)
            }
        }
        
        return result
    }

    private fun stripHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun getStandardChapterCount(bookId: String): Int {
        return when (bookId.uppercase()) {
            "GEN" -> 50; "EXO" -> 40; "LEV" -> 27; "NUM" -> 36; "DEU" -> 34; "JOS" -> 24; "JDG" -> 21; "RUT" -> 4
            "1SA" -> 31; "2SA" -> 24; "1KI" -> 22; "2KI" -> 25; "1CH" -> 29; "2CH" -> 36; "EZR" -> 10; "NEH" -> 13
            "EST" -> 10; "JOB" -> 42; "PSA" -> 150; "PRO" -> 31; "ECC" -> 12; "SNG" -> 8; "ISA" -> 66; "JER" -> 52
            "LAM" -> 5; "EZK" -> 48; "DAN" -> 12; "HOS" -> 14; "JOL" -> 3; "AMO" -> 9; "OBA" -> 1; "JON" -> 4
            "MIC" -> 7; "NAM" -> 3; "HAB" -> 3; "ZEP" -> 3; "HAG" -> 2; "ZEC" -> 14; "MAL" -> 4
            "MAT" -> 28; "MRK" -> 16; "LUK" -> 24; "JHN" -> 21; "ACT" -> 28; "ROM" -> 16; "1CO" -> 16; "2CO" -> 13
            "GAL" -> 6; "EPH" -> 6; "PHP" -> 4; "COL" -> 4; "1TH" -> 5; "2TH" -> 3; "1TI" -> 6; "2TI" -> 4
            "TIT" -> 3; "PHM" -> 1; "HEB" -> 13; "JAS" -> 5; "1PE" -> 5; "2PE" -> 3; "1JN" -> 5; "2JN" -> 1
            "3JN" -> 1; "JUD" -> 1; "REV" -> 22
            else -> 10
        }
    }
}
