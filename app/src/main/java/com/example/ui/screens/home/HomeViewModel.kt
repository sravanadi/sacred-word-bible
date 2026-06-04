package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TranslationEntity
import com.example.data.local.entity.VerseEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.PrayerEntity
import com.example.data.repository.BibleRepository
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed interface HomeUiState {
    object Loading : HomeUiState
    object Empty : HomeUiState
    data class Success(
        val activeTranslation: TranslationEntity,
        val verse: VerseEntity,
        val bookName: String,
        val isBookmarked: Boolean,
        val activeHighlightColorHex: String?
    ) : HomeUiState
}

data class UserPreferences(
    val activeTranslationId: String?,
    val dailyVerseDate: String?,
    val dailyVerseRefId: String?
)

class HomeViewModel(
    private val bibleRepository: BibleRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    val allNotes: StateFlow<List<NoteEntity>> = bibleRepository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPrayers: StateFlow<List<PrayerEntity>> = bibleRepository.allPrayers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            bibleRepository.insertNote(title, content)
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            bibleRepository.deleteNote(id)
        }
    }

    fun addPrayer(title: String, content: String) {
        viewModelScope.launch {
            bibleRepository.insertPrayer(title, content)
        }
    }

    fun deletePrayer(id: Long) {
        viewModelScope.launch {
            bibleRepository.deletePrayer(id)
        }
    }

    fun togglePrayerAnswered(id: Long, isAnswered: Boolean) {
        viewModelScope.launch {
            bibleRepository.updatePrayerStatus(id, isAnswered)
        }
    }

    init {
        loadDailyVerse()
    }

    fun loadDailyVerse() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading

            combine(
                bibleRepository.downloadedTranslations,
                userDataRepository.activeTranslationId,
                bibleRepository.allBookmarks,
                bibleRepository.allHighlights
            ) { downloaded, activeId, bookmarks, highlights ->
                if (downloaded.isEmpty()) {
                    HomeUiState.Empty
                } else {
                    val activeEntity = downloaded.firstOrNull { it.id == activeId } ?: downloaded.first()
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    var activeVerse: VerseEntity? = null
                    var bookName = ""

                    // Deterministic offset based on the calendar date hash and the total verse count
                    val count = bibleRepository.getVersesCount(activeEntity.id)
                    if (count > 0) {
                        val dateHash = (todayStr + "_" + activeEntity.id).hashCode()
                        val offset = kotlin.math.abs(dateHash) % count
                        val verseObj = bibleRepository.getVerseAtOffset(activeEntity.id, offset)
                        if (verseObj != null) {
                            activeVerse = verseObj
                            val book = bibleRepository.getBook(activeEntity.id, verseObj.bookId)
                            bookName = book?.name ?: verseObj.bookId
                        }
                    }

                    if (activeVerse == null) {
                        val fallback = bibleRepository.getRandomVerse(activeEntity.id) ?: bibleRepository.getAnyVerse()
                        if (fallback != null) {
                            activeVerse = fallback
                            val book = bibleRepository.getBook(activeEntity.id, fallback.bookId)
                            bookName = book?.name ?: fallback.bookId
                        }
                    }

                    if (activeVerse == null) {
                        HomeUiState.Empty
                    } else {
                        val isBookmarked = bookmarks.any { it.verseId == activeVerse.id }
                        val highlightHex = highlights.find { it.verseId == activeVerse.id }?.colorHex

                        HomeUiState.Success(
                            activeTranslation = activeEntity,
                            verse = activeVerse,
                            bookName = bookName,
                            isBookmarked = isBookmarked,
                            activeHighlightColorHex = highlightHex
                        )
                    }
                }
            }
            .flowOn(Dispatchers.IO)
            .collectLatest { state ->
                _uiState.value = state
            }
        }
    }

    fun toggleBookmark(verse: VerseEntity, bookName: String) {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is HomeUiState.Success) {
                if (state.isBookmarked) {
                    bibleRepository.removeBookmark(verse.id)
                } else {
                    bibleRepository.bookmarkVerse(verse, bookName)
                }
            }
        }
    }

    fun highlightVerse(verseId: String, colorHex: String) {
        viewModelScope.launch {
            bibleRepository.setHighlight(verseId, colorHex)
        }
    }

    fun removeHighlight(verseId: String) {
        viewModelScope.launch {
            bibleRepository.removeHighlight(verseId)
        }
    }
}
