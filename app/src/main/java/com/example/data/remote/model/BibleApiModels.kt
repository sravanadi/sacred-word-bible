package com.example.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiTranslationsResponse(
    @Json(name = "translations") val translations: List<ApiTranslation>
)

@JsonClass(generateAdapter = true)
data class ApiTranslation(
    @Json(name = "id") val id: String,
    @Json(name = "shortName") val shortName: String?,
    @Json(name = "englishName") val englishName: String?,
    @Json(name = "localName") val localName: String?,
    @Json(name = "language") val language: String?,
    @Json(name = "textDirection") val textDirection: String?
)

@JsonClass(generateAdapter = true)
data class ApiBibleResponse(
    @Json(name = "translation") val translation: ApiTranslation?,
    @Json(name = "books") val books: List<ApiBook>?
)

@JsonClass(generateAdapter = true)
data class ApiBook(
    @Json(name = "id") val id: String, // e.g. "GEN"
    @Json(name = "name") val name: String,
    @Json(name = "commonName") val commonName: String?,
    @Json(name = "bookOrder") val bookOrder: Int?,
    @Json(name = "order") val order: Int? = null,
    @Json(name = "numberOfChapters") val numberOfChapters: Int? = null,
    @Json(name = "chapters") val chapters: List<ApiChapterWrapper>? = null
)

@JsonClass(generateAdapter = true)
data class ApiChapterWrapper(
    @Json(name = "chapter") val chapter: ApiChapterDetails?
)

@JsonClass(generateAdapter = true)
data class ApiChapterDetails(
    @Json(name = "number") val number: Int,
    @Json(name = "content") val content: List<ApiContentItem>?
)

@JsonClass(generateAdapter = true)
data class ApiContentItem(
    @Json(name = "type") val type: String?,
    @Json(name = "number") val number: Int?,
    @Json(name = "text") val text: String?,
    @Json(name = "content") val content: List<Any>?
)

// --- Scripture.API.Bible Models ---

@JsonClass(generateAdapter = true)
data class ScriptureBiblesResponse(
    @Json(name = "data") val data: List<ScriptureBible>
)

@JsonClass(generateAdapter = true)
data class ScriptureBible(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "nameLocal") val nameLocal: String?,
    @Json(name = "abbreviation") val abbreviation: String?,
    @Json(name = "abbreviationLocal") val abbreviationLocal: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "descriptionLocal") val descriptionLocal: String?,
    @Json(name = "language") val language: ScriptureLanguage?
)

@JsonClass(generateAdapter = true)
data class ScriptureLanguage(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "nameLocal") val nameLocal: String?,
    @Json(name = "scriptDirection") val scriptDirection: String?
)

@JsonClass(generateAdapter = true)
data class ScriptureBooksResponse(
    @Json(name = "data") val data: List<ScriptureBook>
)

@JsonClass(generateAdapter = true)
data class ScriptureBook(
    @Json(name = "id") val id: String,
    @Json(name = "bibleId") val bibleId: String,
    @Json(name = "name") val name: String,
    @Json(name = "nameLocal") val nameLocal: String?,
    @Json(name = "abbreviation") val abbreviation: String?,
    @Json(name = "description") val description: String?
)

@JsonClass(generateAdapter = true)
data class ScriptureChaptersResponse(
    @Json(name = "data") val data: List<ScriptureChapterSummary>
)

@JsonClass(generateAdapter = true)
data class ScriptureChapterSummary(
    @Json(name = "id") val id: String,
    @Json(name = "bibleId") val bibleId: String,
    @Json(name = "bookId") val bookId: String,
    @Json(name = "number") val number: String,
    @Json(name = "reference") val reference: String?
)

@JsonClass(generateAdapter = true)
data class ScriptureChapterResponse(
    @Json(name = "data") val data: ScriptureChapterDetails
)

@JsonClass(generateAdapter = true)
data class ScriptureChapterDetails(
    @Json(name = "id") val id: String,
    @Json(name = "bibleId") val bibleId: String,
    @Json(name = "bookId") val bookId: String,
    @Json(name = "number") val number: String,
    @Json(name = "content") val content: String?,
    @Json(name = "reference") val reference: String?,
    @Json(name = "verseCount") val verseCount: Int?
)

