package com.example.diabeteslogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabeteslogger.data.preferences.SettingsDataStore
import com.example.diabeteslogger.util.LocaleManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val language =
        settingsDataStore.languageFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "system"
        )

    fun setLanguage(language: String) {

        viewModelScope.launch {

            // 1. Save preference
            settingsDataStore.setLanguage(language)

            // 2. Apply immediately to Android system
            LocaleManager.applyLanguage(language)
        }
    }
}