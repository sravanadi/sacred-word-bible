package com.example

import com.example.data.remote.BibleApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDownloadBibleParsing() = runBlocking {
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val apiService = Retrofit.Builder()
        .baseUrl("https://bible.helloao.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(BibleApiService::class.java)

        println("Fetching BSB books...")
        val response = apiService.getBooks("BSB")
        println("BSB books parsed successfully!")
        
        val firstBook = response.books?.firstOrNull()
        println("Book: ${firstBook?.name}")
        assertNotNull(firstBook)
        
        val chapterWrapper = apiService.getChapter("BSB", firstBook!!.id, 1)
        val firstChapter = chapterWrapper.chapter
        println("Chapter: ${firstChapter?.number}")
        
        var verseCount = 0
        firstChapter?.content?.forEach { item ->
            if (item.type == "verse") {
                verseCount++
                val textParts = item.content?.mapNotNull { part ->
                    when (part) {
                        is String -> part
                        is Map<*, *> -> {
                            part["text"] as? String
                        }
                        else -> null
                    }
                }?.joinToString("") ?: item.text ?: ""
                
                if (item.number == 1 || item.number == 27) {
                    println("  Verse ${item.number}: $textParts")
                }
            }
        }
        println("Total verses in Genesis 1: $verseCount")
        assertEquals(31, verseCount)
  }

  @Test
  fun testListTranslations() = runBlocking {
    val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val apiService = Retrofit.Builder()
        .baseUrl("https://bible.helloao.org/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(BibleApiService::class.java)

    try {
        val response = apiService.getAvailableTranslations()
        println("=== HelloAO Available Translations ===")
        response.translations.forEach { trans ->
            if (trans.language?.contains("tel") == true || trans.englishName?.contains("Telugu") == true || trans.localName?.contains("Telugu") == true) {
                println("Found Telugu Translation: ID=${trans.id}, shortName=${trans.shortName}, englishName=${trans.englishName}, language=${trans.language}")
                
                // Let's also check if it downloads in Telugu
                val booksResponse = apiService.getBooks(trans.id)
                val book = booksResponse.books?.firstOrNull()
                println("  First Book Local Name: ${book?.name}")
                assertNotNull(book)
                val chapterWrapper = apiService.getChapter(trans.id, book!!.id, 1)
                val firstChap = chapterWrapper.chapter
                val firstVerse = firstChap?.content?.find { it.type == "verse" }
                val textParts = firstVerse?.content?.mapNotNull { part ->
                    when (part) {
                        is String -> part
                        is Map<*, *> -> part["text"] as? String
                        else -> null
                    }
                }?.joinToString("") ?: firstVerse?.text ?: ""
                println("  Verse 1 Text: $textParts")
            }
        }
        println("======================================")
    } catch (e: Exception) {
        e.printStackTrace()
    }
  }
}

