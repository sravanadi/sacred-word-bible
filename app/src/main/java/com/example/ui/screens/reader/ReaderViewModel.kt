package com.example.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.repository.BibleRepository
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReaderUiState(
    val translation: TranslationEntity? = null,
    val book: BookEntity? = null,
    val textDirection: String = "ltr",
    val fontSize: Int = 18,
    val isBookmarkedMap: Map<String, Boolean> = emptyMap(),
    val highlightMap: Map<String, String> = emptyMap() // verseId -> colorHex
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ReaderViewModel(
    private val bibleRepository: BibleRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _translationId = MutableStateFlow<String?>(null)
    private val _bookId = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState = _uiState.asStateFlow()

    private val _currentChapter = MutableStateFlow(1)
    val currentChapter = _currentChapter.asStateFlow()

    private val _verses = MutableStateFlow<List<VerseEntity>>(emptyList())
    val verses = _verses.asStateFlow()

    init {
        // Fetch font size preference
        viewModelScope.launch {
            userDataRepository.readerFontSize.collect { size ->
                _uiState.update { it.copy(fontSize = size) }
            }
        }

        // Fetch translation details (for RTL support)
        viewModelScope.launch {
            combine(_translationId.filterNotNull(), bibleRepository.allTranslations) { tId, translations ->
                translations.find { it.id == tId }
            }.distinctUntilChanged().collectLatest { trans ->
                _uiState.update { 
                    it.copy(
                        translation = trans, 
                        textDirection = trans?.textDirection ?: "ltr"
                    ) 
                }
            }
        }

        // Fetch book details
        viewModelScope.launch {
            combine(_translationId.filterNotNull(), _bookId.filterNotNull()) { tId, bId ->
                tId to bId
            }.distinctUntilChanged().collectLatest { (tId, bId) ->
                val bookObj = bibleRepository.getBook(tId, bId)
                _uiState.update { it.copy(book = bookObj) }
            }
        }

        // Observe verses for current active chapter
        viewModelScope.launch {
            val activeChapterFlow = combine(
                _translationId.filterNotNull(),
                _bookId.filterNotNull(),
                _currentChapter
            ) { tId, bId, ch ->
                Triple(tId, bId, ch)
            }.distinctUntilChanged()

            // Fetch and cache the API.Bible chapter in background if needed
            launch {
                activeChapterFlow.collectLatest { (tId, bId, ch) ->
                    bibleRepository.ensureChapterCached(tId, bId, ch)
                }
            }

            activeChapterFlow.flatMapLatest { (tId, bId, ch) ->
                bibleRepository.getVerses(tId, bId, ch)
            }.collect { list ->
                _verses.value = list
            }
        }

        // Collect all bookmarks to keep the icon in toolbar & verse list active
        viewModelScope.launch {
            bibleRepository.allBookmarks.collectLatest { bookmarks ->
                val map = bookmarks.associate { it.verseId to true }
                _uiState.update { it.copy(isBookmarkedMap = map) }
            }
        }

        // Collect all highlights matching this book & chapter
        viewModelScope.launch {
            bibleRepository.allHighlights.collectLatest { highlights ->
                val map = highlights.associate { it.verseId to it.colorHex }
                _uiState.update { it.copy(highlightMap = map) }
            }
        }
    }

    fun initialize(tId: String, bId: String, initialChapter: Int) {
        _translationId.value = tId
        _bookId.value = bId
        _currentChapter.value = initialChapter
    }

    fun setChapter(chapter: Int) {
        val total = _uiState.value.book?.totalChapters ?: 1
        if (chapter in 1..total) {
            _currentChapter.value = chapter
        }
    }

    fun nextChapter() {
        val maxChapters = _uiState.value.book?.totalChapters ?: 1
        if (_currentChapter.value < maxChapters) {
            _currentChapter.value += 1
        }
    }

    fun previousChapter() {
        if (_currentChapter.value > 1) {
            _currentChapter.value -= 1
        }
    }

    fun adjustFontSize(increment: Boolean) {
        viewModelScope.launch {
            val currentSize = _uiState.value.fontSize
            val newSize = if (increment) currentSize + 2 else currentSize - 2
            userDataRepository.setReaderFontSize(newSize)
        }
    }

    fun toggleBookmark(verse: VerseEntity) {
        viewModelScope.launch {
            val bookName = _uiState.value.book?.name ?: "Book"
            val isBookmarked = _uiState.value.isBookmarkedMap[verse.id] ?: false
            if (isBookmarked) {
                bibleRepository.removeBookmark(verse.id)
            } else {
                bibleRepository.bookmarkVerse(verse, bookName)
            }
        }
    }

    fun isVerseBookmarked(verseId: String): Boolean {
        return _uiState.value.isBookmarkedMap[verseId] ?: false
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
