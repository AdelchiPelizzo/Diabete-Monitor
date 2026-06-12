package com.example.diabeteslogger.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import com.example.diabeteslogger.ui.viewmodel.FilterType
import com.github.mikephil.charting.data.Entry

@Composable
fun HomeScreen(
    viewModel: LogViewModel,
    modifier: Modifier = Modifier
) {

    // ✅ FIX 1: collect state correctly
    val entries by viewModel.entries.collectAsState()
    val filter by viewModel.filter.collectAsState()

    // ✅ FIX 2: filter locally (NO getFilteredEntries)
    val filteredEntries = remember(entries, filter) {
        val now = System.currentTimeMillis()

        when (filter) {
            FilterType.TODAY -> {
                val start = now - 24 * 60 * 60 * 1000
                entries.filter { it.timestamp >= start }
            }

            FilterType.WEEK -> {
                val week = now - 7 * 24 * 60 * 60 * 1000
                entries.filter { it.timestamp >= week }
            }
        }.sortedBy { it.timestamp }
    }

    // ✅ FIX 3: chart mapping
    val chartEntries = remember(filteredEntries) {
        filteredEntries.mapIndexed { index, item ->
            Entry(index.toFloat(), item.value.toFloat())
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    Column(modifier = modifier.padding(16.dp)) {

        Text("Glucose Tracker", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        Row {
            Button(onClick = { viewModel.setFilter(FilterType.TODAY) }) {
                Text("Today")
            }

            Spacer(Modifier.width(8.dp))

            Button(onClick = { viewModel.setFilter(FilterType.WEEK) }) {
                Text("Week")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ⚠️ FIX: chart MUST receive data, not viewModel
        GlucoseChart(entries = chartEntries)

        Spacer(Modifier.height(16.dp))

        Button(onClick = { showDialog = true }) {
            Text("Add glucose")
        }

        Spacer(Modifier.height(16.dp))

        if (filteredEntries.isEmpty()) {
            Text("No entries yet")
        } else {
            filteredEntries.forEach {
                Text("Glucose: ${it.value} mg/dL")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add glucose") },
            text = {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("mg/dL") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    inputText.toIntOrNull()?.let { value ->
                        viewModel.addEntry(value)
                    }
                    inputText = ""
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}