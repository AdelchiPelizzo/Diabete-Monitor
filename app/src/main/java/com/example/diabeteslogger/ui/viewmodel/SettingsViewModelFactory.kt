package com.example.diabeteslogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.diabeteslogger.data.repository.GlucoseRepository
import com.example.diabeteslogger.data.preferences.SettingsDataStore

class SettingsViewModelFactory(
    private val settingsDataStore: SettingsDataStore,
    private val glucoseRepository: GlucoseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                settingsDataStore,
                glucoseRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}