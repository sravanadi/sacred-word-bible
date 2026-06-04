package com.example.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.SacredWordApplication
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.bible.BibleViewModel
import com.example.ui.screens.reader.ReaderViewModel
import com.example.ui.screens.library.LibraryViewModel
import com.example.ui.screens.bookmarks.BookmarksViewModel
import com.example.ui.screens.settings.SettingsViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            // Retrieve application instance
            val application = (extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? SacredWordApplication)
                ?: SacredWordApplication.instance
            val bibleRepo = application.container.bibleRepository
            val userRepo = application.container.userDataRepository

            return when {
                modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                    HomeViewModel(bibleRepo, userRepo) as T
                }
                modelClass.isAssignableFrom(BibleViewModel::class.java) -> {
                    BibleViewModel(bibleRepo, userRepo) as T
                }
                modelClass.isAssignableFrom(ReaderViewModel::class.java) -> {
                    ReaderViewModel(bibleRepo, userRepo) as T
                }
                modelClass.isAssignableFrom(LibraryViewModel::class.java) -> {
                    LibraryViewModel(bibleRepo, userRepo) as T
                }
                modelClass.isAssignableFrom(BookmarksViewModel::class.java) -> {
                    BookmarksViewModel(bibleRepo) as T
                }
                modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                    SettingsViewModel(userRepo) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
