package com.example.data.remote

import com.example.data.remote.model.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ScriptureApiService {
    @GET("bibles")
    suspend fun getBibles(
        @Header("api-key") apiKey: String
    ): ScriptureBiblesResponse

    @GET("bibles/{bibleId}/books")
    suspend fun getBooks(
        @Header("api-key") apiKey: String,
        @Path("bibleId") bibleId: String
    ): ScriptureBooksResponse

    @GET("bibles/{bibleId}/books/{bookId}/chapters")
    suspend fun getChapters(
        @Header("api-key") apiKey: String,
        @Path("bibleId") bibleId: String,
        @Path("bookId") bookId: String
    ): ScriptureChaptersResponse

    @GET("bibles/{bibleId}/chapters/{chapterId}")
    suspend fun getChapterContent(
        @Header("api-key") apiKey: String,
        @Path("bibleId") bibleId: String,
        @Path("chapterId") chapterId: String,
        @Query("content-type") contentType: String = "html",
        @Query("include-vnums") includeVnums: Boolean = true
    ): ScriptureChapterResponse
}
