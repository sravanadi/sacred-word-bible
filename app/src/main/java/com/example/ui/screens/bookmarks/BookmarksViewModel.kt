package com.example.ui.screens.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BookmarkEntity
import com.example.data.repository.BibleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BookmarksViewModel(
    private val bibleRepository: BibleRepository
) : ViewModel() {

    val bookmarks: StateFlow<List<BookmarkEntity>> = bibleRepository.allBookmarks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeBookmark(verseId: String) {
        viewModelScope.launch {
            bibleRepository.removeBookmark(verseId)
        }
    }
}
