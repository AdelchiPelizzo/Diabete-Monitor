package com.example.diabeteslogger.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import com.example.diabeteslogger.ui.viewmodel.FilterType
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LogViewModel,
    modifier: Modifier = Modifier
) {

    var showDatePicker by remember { mutableStateOf(false) }
    var tempStart by remember { mutableStateOf<Long?>(null) }
    var isPickingStart by remember { mutableStateOf(true) }

    val filteredEntries by viewModel.filteredEntries.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    // ---------------- DAY GROUPING ----------------
    val groupedByDay = remember(filteredEntries) {
        filteredEntries.groupBy { entry ->
            Calendar.getInstance().apply {
                timeInMillis = entry.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.toSortedMap()
    }

    val dayLabels = remember(groupedByDay) {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        groupedByDay.keys.map { sdf.format(Date(it)) }
    }

    // ---------------- AM / PM ----------------
    val morningEntries = remember(groupedByDay) {
        groupedByDay.entries.mapIndexedNotNull { index, (_, list) ->
            list.sortedBy { it.timestamp }
                .firstOrNull()
                ?.let { Entry(index.toFloat(), it.value.toFloat()) }
        }
    }

    val eveningEntries = remember(groupedByDay) {
        groupedByDay.entries.mapIndexedNotNull { index, (_, list) ->
            list.sortedBy { it.timestamp }
                .getOrNull(1)
                ?.let { Entry(index.toFloat(), it.value.toFloat()) }
        }
    }

    val averageEntries = remember(groupedByDay) {
        groupedByDay.entries.mapIndexed { index, (_, list) ->
            Entry(index.toFloat(), list.map { it.value }.average().toFloat())
        }
    }

    Column(modifier = modifier.padding(16.dp)) {

        Text("Glucose Tracker", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {

            val options = listOf("Week", "Month", "Year", "Range")

            options.forEachIndexed { index, label ->

                val selected = when (label) {
                    "Week" -> currentFilter == FilterType.WEEK
                    "Month" -> currentFilter == FilterType.MONTH
                    "Year" -> currentFilter == FilterType.YEAR
                    "Range" -> currentFilter == FilterType.CUSTOM
                    else -> false
                }

                SegmentedButton(
                    selected = selected,
                    onClick = {
                        when (label) {
                            "Week" -> viewModel.setFilter(FilterType.WEEK)
                            "Month" -> viewModel.setFilter(FilterType.MONTH)
                            "Year" -> viewModel.setFilter(FilterType.YEAR)

                            "Range" -> {
                                viewModel.setFilter(FilterType.CUSTOM)
                                showDatePicker = true
                                isPickingStart = true
                            }
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size)
                ) {
                    Text(label)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        GlucoseChart(
            morningEntries = morningEntries,
            eveningEntries = eveningEntries,
            averageEntries = averageEntries,
            labels = dayLabels
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add glucose")
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Text("Export CSV")
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(Modifier.height(350.dp)) {

            groupedByDay.forEach { (dayMillis, list) ->

                item {

                    val dateLabel = SimpleDateFormat("dd/MM", Locale.getDefault())
                        .format(Date(dayMillis))

                    Column(Modifier.padding(vertical = 10.dp)) {

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text("📅 $dateLabel", Modifier.padding(12.dp))
                        }

                        val sorted = list.sortedBy { it.timestamp }

                        sorted.getOrNull(0)?.let {
                            SwipeRow("🌅 Morning", it) { viewModel.deleteEntry(it) }
                        }

                        sorted.getOrNull(1)?.let {
                            SwipeRow("🌙 Evening", it) { viewModel.deleteEntry(it) }
                        }
                    }
                }
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
                    onValueChange = { inputText = it.filter(Char::isDigit) },
                    label = { Text("mg/dL") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    inputText.toIntOrNull()?.let { viewModel.addEntry(it) }
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

    val state = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
                tempStart = null
            },
            confirmButton = {
                Button(onClick = {
                    val selected = state.selectedDateMillis ?: return@Button

                    if (isPickingStart) {
                        tempStart = selected
                        isPickingStart = false
                    } else {
                        val start = tempStart ?: selected
                        val end = selected
                        viewModel.setDateRange(minOf(start, end), maxOf(start, end))

                        showDatePicker = false
                        tempStart = null
                        isPickingStart = true
                    }
                }) {
                    Text(if (isPickingStart) "Next" else "Apply")
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun SwipeRow(
    label: String,
    entry: com.example.diabeteslogger.data.local.GlucoseEntry,
    onDelete: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = state,
        backgroundContent = {},
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("$label: ${entry.value}")
                    Text(
                        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(entry.timestamp))
                    )
                }
            }
        }
    )
}