package com.example.ui.screens.bible

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BookEntity
import com.example.data.local.entity.TranslationEntity
import com.example.data.repository.BibleRepository
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface BibleUiState {
    object Empty : BibleUiState
    data class Success(
        val activeTranslation: TranslationEntity,
        val downloadedTranslations: List<TranslationEntity>,
        val otBooks: List<BookEntity>,
        val ntBooks: List<BookEntity>
    ) : BibleUiState
}

class BibleViewModel(
    private val bibleRepository: BibleRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    // Expose active translation ID
    val activeTranslationId: StateFlow<String?> = userDataRepository.activeTranslationId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Main UI state
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BibleUiState> = combine(
        bibleRepository.downloadedTranslations,
        activeTranslationId
    ) { downloaded, activeId ->
        downloaded to activeId
    }.flatMapLatest { (downloaded, activeId) ->
        if (downloaded.isEmpty()) {
            flowOf<BibleUiState>(BibleUiState.Empty)
        } else {
            // Find current active translation entity
            val activeEntity = downloaded.firstOrNull { it.id == activeId } 
                ?: downloaded.first() // Fallback to first downloaded
            
            // If the active translation ID didn't match or was blank, sync it
            if (activeId != activeEntity.id) {
                viewModelScope.launch {
                    userDataRepository.setActiveTranslationId(activeEntity.id)
                }
            }

            // Load books for this translation as flow (runs non-blockingly)
            bibleRepository.getBooks(activeEntity.id).map<List<BookEntity>, BibleUiState> { books ->
                // Categorize into Old Testament and New Testament
                // Conventionally: first 39 books are OT, others are NT
                val ot = books.filter { it.bookOrder <= 39 }
                val nt = books.filter { it.bookOrder > 39 }

                BibleUiState.Success(
                    activeTranslation = activeEntity,
                    downloadedTranslations = downloaded,
                    otBooks = ot,
                    ntBooks = nt
                )
            }
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BibleUiState.Empty
    )

    fun setActiveTranslation(translationId: String) {
        viewModelScope.launch {
            userDataRepository.setActiveTranslationId(translationId)
        }
    }
}
