package com.example.diabeteslogger.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.ui.res.stringResource
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import com.example.diabeteslogger.ui.viewmodel.FilterType
import com.github.mikephil.charting.data.Entry
import java.text.SimpleDateFormat
import java.util.*
import com.example.diabeteslogger.R
import androidx.compose.ui.text.input.KeyboardType

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

        Text(stringResource(R.string.glucose_tracker), style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {

            val options = listOf(
                FilterType.WEEK,
                FilterType.MONTH,
                FilterType.YEAR,
                FilterType.CUSTOM
            )

            options.forEachIndexed { index, filterType ->

                val selected = currentFilter == filterType

                val label = when (filterType) {
                    FilterType.WEEK ->
                        stringResource(R.string.filter_week)

                    FilterType.MONTH ->
                        stringResource(R.string.filter_month)

                    FilterType.YEAR ->
                        stringResource(R.string.filter_year)

                    FilterType.CUSTOM ->
                        stringResource(R.string.filter_range)
                }

                SegmentedButton(
                    selected = selected,
                    onClick = {

                        if (filterType == FilterType.CUSTOM) {

                            viewModel.setFilter(FilterType.CUSTOM)

                            showDatePicker = true
                            isPickingStart = true

                        } else {

                            viewModel.setFilter(filterType)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index,
                        options.size
                    )
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
                Text(stringResource(R.string.add_reading))
            }

            OutlinedButton(
                onClick = { },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.export_csv))
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
                            SwipeRow(
                                "🌅 ${stringResource(R.string.morning)}",
                                it
                            ) {
                                viewModel.deleteEntry(it)
                            }
                        }

                        sorted.getOrNull(1)?.let {
                            SwipeRow(
                                "🌙 ${stringResource(R.string.evening)}",
                                it
                            ) {
                                viewModel.deleteEntry(it)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(stringResource(R.string.dialog_add_glucose))
            },
            text = {OutlinedTextField(
                value = inputText,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        inputText = newValue
                    }
                },
                label = {
                    Text(stringResource(R.string.glucose_unit))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
            },
            confirmButton = {
                Button(onClick = {
                    inputText.toIntOrNull()?.let { viewModel.addEntry(it) }
                    inputText = ""
                    showDialog = false
                }) {
                    Text(stringResource(R.string.dialog_save))
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
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
                    Text(
                        if (isPickingStart)
                            stringResource(R.string.date_next)
                        else
                            stringResource(R.string.date_apply)
                    )
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