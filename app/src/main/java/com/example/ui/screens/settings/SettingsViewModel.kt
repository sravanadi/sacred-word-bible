package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    val isDarkTheme: StateFlow<Boolean> = userDataRepository.isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val fontFamilyName: StateFlow<String> = userDataRepository.fontFamilyName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "sans"
        )

    val isLiveAdsEnabled: StateFlow<Boolean> = userDataRepository.isLiveAdsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setLiveAdsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userDataRepository.setLiveAdsEnabled(enabled)
        }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch {
            userDataRepository.setDarkTheme(isDark)
        }
    }

    fun setFontFamilyName(name: String) {
        viewModelScope.launch {
            userDataRepository.setFontFamilyName(name)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            userDataRepository.setCompletedOnboarding(false)
        }
    }
}
