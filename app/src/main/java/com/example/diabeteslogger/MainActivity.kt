package com.example.diabeteslogger

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope

import com.example.diabeteslogger.data.local.AppDatabase
import com.example.diabeteslogger.data.preferences.SettingsDataStore
import com.example.diabeteslogger.data.repository.GlucoseRepository

import com.example.diabeteslogger.ui.MainScreen
import com.example.diabeteslogger.ui.splash.SplashScreen

import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import com.example.diabeteslogger.ui.viewmodel.LogViewModelFactory
import com.example.diabeteslogger.ui.viewmodel.SettingsViewModel
import com.example.diabeteslogger.ui.viewmodel.SettingsViewModelFactory

import com.example.diabeteslogger.util.LocaleManager

import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------------- DATABASE + REPOSITORY ----------------
        val dao = AppDatabase.getDatabase(applicationContext).glucoseDao()
        val glucoseRepository = GlucoseRepository(dao)

        // ---------------- DATASTORE ----------------
        val settingsDataStore = SettingsDataStore(applicationContext)

        // ---------------- LOG VIEWMODEL ----------------
        val logViewModel: LogViewModel by viewModels {
            LogViewModelFactory(glucoseRepository)
        }

        // ---------------- SETTINGS VIEWMODEL ----------------
        val settingsViewModel = SettingsViewModel(
            settingsDataStore = settingsDataStore,
            glucoseRepository = glucoseRepository
        )

        // ---------------- LANGUAGE OBSERVER ----------------
        lifecycleScope.launch {
            settingsDataStore.languageFlow.collect { lang ->

                val locales = when (lang) {
                    "it" -> LocaleListCompat.forLanguageTags("it")
                    "en" -> LocaleListCompat.forLanguageTags("en")
                    else -> LocaleListCompat.getEmptyLocaleList()
                }

                AppCompatDelegate.setApplicationLocales(locales)
                LocaleManager.applyLanguage(lang)
            }
        }

        // ---------------- UI ----------------
        setContent {

            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showSplash = false
            }

            if (showSplash) {

                SplashScreen(
                    onFinished = { showSplash = false }
                )

            } else {

                Scaffold { innerPadding ->
                    MainScreen(
                        viewModel = logViewModel,
                        settingsViewModel = settingsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}