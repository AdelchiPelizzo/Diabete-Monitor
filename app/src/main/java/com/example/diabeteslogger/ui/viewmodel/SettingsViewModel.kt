package com.example.diabeteslogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabeteslogger.data.preferences.SettingsDataStore
import com.example.diabeteslogger.data.repository.GlucoseRepository
import com.example.diabeteslogger.util.LocaleManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import com.example.diabeteslogger.data.local.GlucoseEntry

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val glucoseRepository: GlucoseRepository
) : ViewModel() {

    // ---------------- LANGUAGE STATE ----------------
    val language =
        settingsDataStore.languageFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "system"
        )

    // ---------------- GLUCOSE DATA (FOR BACKUP) ----------------
    val glucoseEntries: Flow<List<GlucoseEntry>> =
        glucoseRepository.getAll()

    // ---------------- LANGUAGE UPDATE ----------------
    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
            LocaleManager.applyLanguage(language)
        }
    }
}