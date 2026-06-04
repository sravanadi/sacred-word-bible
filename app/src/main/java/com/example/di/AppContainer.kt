package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.SacredWordDatabase
import com.example.data.remote.BibleApiService
import com.example.data.remote.ScriptureApiService
import com.example.data.repository.BibleRepository
import com.example.data.repository.UserDataRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val bibleRepository: BibleRepository
    val userDataRepository: UserDataRepository
}

class AppContainerImpl(private val context: Context) : AppContainer {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (com.example.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()

    private val apiService: BibleApiService = Retrofit.Builder()
        .baseUrl("https://bible.helloao.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(BibleApiService::class.java)

    private val scriptureApiService: ScriptureApiService = Retrofit.Builder()
        .baseUrl("https://api.scripture.api.bible/v1/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(ScriptureApiService::class.java)

    private val database: SacredWordDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            SacredWordDatabase::class.java,
            "sacred_word_database"
        )
        .fallbackToDestructiveMigration(true) // ensures safety during updates
        .build()
    }

    override val bibleRepository: BibleRepository by lazy {
        BibleRepository(
            apiService = apiService,
            scriptureApiService = scriptureApiService,
            translationDao = database.translationDao(),
            bookDao = database.bookDao(),
            verseDao = database.verseDao(),
            bookmarkDao = database.bookmarkDao(),
            highlightDao = database.highlightDao(),
            noteDao = database.noteDao(),
            prayerDao = database.prayerDao()
        )
    }

    override val userDataRepository: UserDataRepository by lazy {
        UserDataRepository(context)
    }
}
