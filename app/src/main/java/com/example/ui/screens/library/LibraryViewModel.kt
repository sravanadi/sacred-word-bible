package com.example.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TranslationEntity
import com.example.data.repository.BibleRepository
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    object Loading : LibraryUiState
    data class Success(
        val translations: List<TranslationEntity>,
        val downloadedTranslations: List<TranslationEntity>
    ) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

class LibraryViewModel(
    private val bibleRepository: BibleRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _downloadProgressMap = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgressMap = _downloadProgressMap.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage = _snackbarMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            try {
                bibleRepository.fetchAvailableTranslations()
            } catch (e: Exception) {
                _snackbarMessage.emit("Showing list offline. Network unavailable.")
            }
        }
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        bibleRepository.allTranslations,
        bibleRepository.downloadedTranslations,
        searchQuery
    ) { all, downloaded, query ->
        val filtered = if (query.isBlank()) {
            all
        } else {
            all.filter {
                it.englishName.contains(query, ignoreCase = true) ||
                it.localName.contains(query, ignoreCase = true) ||
                it.id.contains(query, ignoreCase = true) ||
                it.language.contains(query, ignoreCase = true)
            }
        }
        LibraryUiState.Success(
            translations = filtered,
            downloadedTranslations = downloaded
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState.Loading
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun downloadTranslation(translationId: String) {
        if (_downloadProgressMap.value.containsKey(translationId)) return

        viewModelScope.launch {
            try {
                _downloadProgressMap.update { it + (translationId to 0.01f) }
                bibleRepository.downloadTranslation(translationId) { progress ->
                    _downloadProgressMap.update { it + (translationId to progress) }
                }
                
                // Auto-set as active if none is active
                val active = userDataRepository.activeTranslationId.firstOrNull()
                if (active.isNullOrBlank()) {
                    userDataRepository.setActiveTranslationId(translationId)
                }
                _snackbarMessage.emit("Download complete ✓")
            } catch (e: Exception) {
                _snackbarMessage.emit("Download failed: ${e.localizedMessage}")
            } finally {
                _downloadProgressMap.update { it - translationId }
            }
        }
    }

    fun deleteTranslation(translationId: String) {
        viewModelScope.launch {
            try {
                bibleRepository.deleteTranslation(translationId)
                
                val currentActive = userDataRepository.activeTranslationId.firstOrNull()
                if (currentActive == translationId) {
                    val remaining = bibleRepository.downloadedTranslations.firstOrNull() ?: emptyList()
                    val nextActive = remaining.firstOrNull { it.id != translationId }?.id ?: ""
                    userDataRepository.setActiveTranslationId(nextActive)
                }
                _snackbarMessage.emit("Deleted $translationId from device")
            } catch (e: Exception) {
                _snackbarMessage.emit("Delete failed: ${e.localizedMessage}")
            }
        }
    }
}
