package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserDataRepository(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")
        private val KEY_ACTIVE_TRANSLATION_ID = stringPreferencesKey("active_translation_id")
        private val KEY_READER_FONT_SIZE = intPreferencesKey("reader_font_size")
        private val KEY_DAILY_VERSE_DATE = stringPreferencesKey("daily_verse_date")
        private val KEY_DAILY_VERSE_SEED = intPreferencesKey("daily_verse_seed")
        private val KEY_DAILY_VERSE_REF_ID = stringPreferencesKey("daily_verse_ref_id")
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_FONT_FAMILY = stringPreferencesKey("font_family")
        private val KEY_COMPLETED_ONBOARDING = booleanPreferencesKey("completed_onboarding")
        private val KEY_LIVE_ADS_ENABLED = booleanPreferencesKey("live_ads_enabled")
    }

    private val dataStoreFlow: Flow<Preferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val isLiveAdsEnabled: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[KEY_LIVE_ADS_ENABLED] ?: true // defaults to true for real-time production ads
    }

    suspend fun setLiveAdsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LIVE_ADS_ENABLED] = enabled
        }
    }

    val completedOnboarding: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[KEY_COMPLETED_ONBOARDING] ?: false
    }

    suspend fun setCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_COMPLETED_ONBOARDING] = completed
        }
    }

    val isDarkTheme: Flow<Boolean> = dataStoreFlow.map { preferences ->
        preferences[KEY_THEME] != "light" // defaults to true (dark mode)
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = if (isDark) "dark" else "light"
        }
    }

    val fontFamilyName: Flow<String> = dataStoreFlow.map { preferences ->
        preferences[KEY_FONT_FAMILY] ?: "sans" // default to sans
    }

    suspend fun setFontFamilyName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FONT_FAMILY] = name
        }
    }

    val activeTranslationId: Flow<String?> = dataStoreFlow.map { preferences ->
        preferences[KEY_ACTIVE_TRANSLATION_ID]
    }

    suspend fun setActiveTranslationId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_TRANSLATION_ID] = id
        }
    }

    val readerFontSize: Flow<Int> = dataStoreFlow.map { preferences ->
        preferences[KEY_READER_FONT_SIZE] ?: 18 // Default 18sp
    }

    suspend fun setReaderFontSize(size: Int) {
        val clampedSize = size.coerceIn(14, 26)
        context.dataStore.edit { preferences ->
            preferences[KEY_READER_FONT_SIZE] = clampedSize
        }
    }

    val dailyVerseDate: Flow<String?> = dataStoreFlow.map { preferences ->
        preferences[KEY_DAILY_VERSE_DATE]
    }

    val dailyVerseSeed: Flow<Int?> = dataStoreFlow.map { preferences ->
        preferences[KEY_DAILY_VERSE_SEED]
    }

    val dailyVerseRefId: Flow<String?> = dataStoreFlow.map { preferences ->
        preferences[KEY_DAILY_VERSE_REF_ID]
    }

    suspend fun saveDailyVerseState(date: String, seed: Int, referenceId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DAILY_VERSE_DATE] = date
            preferences[KEY_DAILY_VERSE_SEED] = seed
            preferences[KEY_DAILY_VERSE_REF_ID] = referenceId
        }
    }
}
