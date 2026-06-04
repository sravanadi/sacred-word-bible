package com.example.data.remote

import com.example.data.remote.model.ApiTranslationsResponse
import com.example.data.remote.model.ApiBibleResponse
import com.example.data.remote.model.ApiChapterWrapper
import retrofit2.http.GET
import retrofit2.http.Path

interface BibleApiService {
    @GET("api/available_translations.json")
    suspend fun getAvailableTranslations(): ApiTranslationsResponse

    @GET("api/{translationId}/books.json")
    suspend fun getBooks(
        @Path("translationId") translationId: String
    ): ApiBibleResponse

    @GET("api/{translationId}/{bookId}/{chapterNumber}.json")
    suspend fun getChapter(
        @Path("translationId") translationId: String,
        @Path("bookId") bookId: String,
        @Path("chapterNumber") chapterNumber: Int
    ): ApiChapterWrapper
}
