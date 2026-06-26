package com.example.diabeteslogger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import com.example.diabeteslogger.data.local.AppDatabase
import com.example.diabeteslogger.data.preferences.SettingsDataStore
import com.example.diabeteslogger.data.repository.GlucoseRepository
import com.example.diabeteslogger.ui.MainScreen
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import com.example.diabeteslogger.ui.viewmodel.LogViewModelFactory
import com.example.diabeteslogger.ui.viewmodel.SettingsViewModel
import com.example.diabeteslogger.util.LocaleManager
import kotlinx.coroutines.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.diabeteslogger.ui.splash.SplashScreen

class MainActivity : AppCompatActivity() {

    private val viewModel: LogViewModel by viewModels {
        val dao = AppDatabase.getDatabase(applicationContext).glucoseDao()
        val repo = GlucoseRepository(dao)
        LogViewModelFactory(repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val dataStore = SettingsDataStore(applicationContext)

        lifecycleScope.launch {
            dataStore.languageFlow.collect { lang ->

                val locales = when (lang) {
                    "it" -> LocaleListCompat.forLanguageTags("it")
                    "en" -> LocaleListCompat.forLanguageTags("en")
                    else -> LocaleListCompat.getEmptyLocaleList()
                }

                AppCompatDelegate.setApplicationLocales(locales)
            }
        }

        val settingsDataStore = SettingsDataStore(this)
        val settingsViewModel = SettingsViewModel(settingsDataStore)

        lifecycleScope.launch {
            settingsDataStore.languageFlow.collect { language ->
                LocaleManager.applyLanguage(language)
            }
        }

        setContent {

            var showSplash by remember {
                mutableStateOf(true)
            }

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
                        viewModel = viewModel,
                        settingsViewModel = settingsViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}