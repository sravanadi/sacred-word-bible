package com.example.ui.screens.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BookEntity
import com.example.data.repository.BibleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChaptersViewModel(
    private val bibleRepository: BibleRepository,
    private val translationId: String,
    private val bookId: String
) : ViewModel() {

    private val _book = MutableStateFlow<BookEntity?>(null)
    val book = _book.asStateFlow()

    private val _highlightedChapters = MutableStateFlow<Set<Int>>(emptySet())
    val highlightedChapters = _highlightedChapters.asStateFlow()

    init {
        viewModelScope.launch {
            val b = bibleRepository.getBook(translationId, bookId)
            _book.value = b
        }

        viewModelScope.launch {
            bibleRepository.allHighlights
                .map { highlights ->
                    val prefix = "${translationId}_${bookId}_"
                    highlights
                        .filter { it.verseId.startsWith(prefix) }
                        .mapNotNull { 
                            val remaining = it.verseId.substring(prefix.length)
                            val parts = remaining.split("_")
                            parts.firstOrNull()?.toIntOrNull()
                        }
                        .toSet()
                }
                .flowOn(Dispatchers.Default)
                .collect { chapters ->
                    _highlightedChapters.value = chapters
                }
        }
    }

    class Factory(
        private val bibleRepository: BibleRepository,
        private val translationId: String,
        private val bookId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChaptersViewModel(bibleRepository, translationId, bookId) as T
        }
    }
}
