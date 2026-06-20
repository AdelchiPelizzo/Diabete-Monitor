package com.example.diabeteslogger.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable

import androidx.compose.material3.Card
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.diabeteslogger.R
import com.example.diabeteslogger.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {

    val selectedLanguage by viewModel.language.collectAsState()
    Column(
        modifier = Modifier.padding(16.dp)
    ) {

//        Text(
//            text = "Selected language: ${selectedLanguage}"
//        )

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
            .clickable(onClick = onClick)
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