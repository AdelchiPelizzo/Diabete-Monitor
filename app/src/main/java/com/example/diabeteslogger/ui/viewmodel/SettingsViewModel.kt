package com.example.diabeteslogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabeteslogger.data.preferences.SettingsDataStore
import com.example.diabeteslogger.data.repository.GlucoseRepository
import com.example.diabeteslogger.data.local.GlucoseEntry
import com.example.diabeteslogger.util.LocaleManager
import com.example.diabeteslogger.util.JsonBackupImporter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore,
    private val glucoseRepository: GlucoseRepository
) : ViewModel() {

    val language =
        settingsDataStore.languageFlow.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "system"
        )

    val glucoseEntries: Flow<List<GlucoseEntry>> =
        glucoseRepository.getAll()

    fun setLanguage(language: String) {
        viewModelScope.launch {
            settingsDataStore.setLanguage(language)
            LocaleManager.applyLanguage(language)
        }
    }

    fun restoreBackup(json: String, replace: Boolean = true) {
        viewModelScope.launch {

            val entries = JsonBackupImporter.import(json)

            if (replace) {
                glucoseRepository.clearAll()
            }

            glucoseRepository.insertAll(entries)
        }
    }
}