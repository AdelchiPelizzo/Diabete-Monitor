package com.example.diabeteslogger.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable

import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import com.example.diabeteslogger.R
import com.example.diabeteslogger.ui.viewmodel.SettingsViewModel
import com.example.diabeteslogger.util.ExportManager
import com.example.diabeteslogger.util.ExportType

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {

    val selectedLanguage by viewModel.language.collectAsState()
    val context = LocalContext.current

    // NOTE: this will still fail until ViewModel exposes it properly
    val entries by viewModel.glucoseEntries.collectAsState(initial = emptyList())

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            ExportManager.export(
                context = context,
                type = ExportType.JSON_BACKUP,
                entries = entries,
                uri = uri
            )
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        // ---------------- LANGUAGE SECTION ----------------

        LanguageOption(
            text = stringResource(R.string.system_default),
            selected = selectedLanguage == "system"
        ) {
            viewModel.setLanguage("system")
        }

        LanguageOption(
            text = stringResource(R.string.english),
            selected = selectedLanguage == "en"
        ) {
            viewModel.setLanguage("en")
        }

        LanguageOption(
            text = stringResource(R.string.italian),
            selected = selectedLanguage == "it"
        ) {
            viewModel.setLanguage("it")
        }

        // ---------------- BACKUP SECTION ----------------

        Text(
            text = "Data Backup",
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    exportLauncher.launch("diabete_monitor_backup.json")
                }
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text("Export JSON Backup")
            }
        }
    }
}

@Composable
private fun LanguageOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {

            RadioButton(
                selected = selected,
                onClick = onClick
            )

            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}