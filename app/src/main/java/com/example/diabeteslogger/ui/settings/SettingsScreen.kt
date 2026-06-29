package com.example.diabeteslogger.ui.settings

import android.R.attr.text
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

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import java.io.BufferedReader

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

    var pendingJson by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }


    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->

        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val json = inputStream?.bufferedReader()?.use { it.readText() }

            if (json != null) {
                pendingJson = json
                showDialog = true
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(
            text = stringResource(R.string.settings)+" ⚙️ ",
            style = MaterialTheme.typography.headlineSmall,
            fontSize = MaterialTheme.typography.headlineMedium.fontSize
        )

        Spacer(modifier = Modifier.height(30.dp))

        // ---------------- LANGUAGE SECTION ----------------

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                text = stringResource(R.string.language),
                style = MaterialTheme.typography.titleLarge,

            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

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
            text = stringResource(R.string.data_backup) + " 💾",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 32.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    exportLauncher.launch("diabete_monitor_backup.json")
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Icon(Icons.Default.Upload, contentDescription = "Export Backup")
                Text(
                    text = stringResource(R.string.export_json_backup),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        if (showDialog && pendingJson != null) {
            RestoreConfirmDialog(
                onConfirm = {
                    viewModel.restoreBackup(pendingJson!!, replace = true)
                    showDialog = false
                    pendingJson = null
                },
                onCancel = {
                    showDialog = false
                    pendingJson = null
                }
            )
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clickable {
                    importLauncher.launch(arrayOf("application/json"))
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.Center
            ) {

                Icon(Icons.Default.Download, contentDescription = "Restore Backup")
                Text(
                    text = stringResource(R.string.restore_json_backup),
                    style = MaterialTheme.typography.bodyLarge
                )
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
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}