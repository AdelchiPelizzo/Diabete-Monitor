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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.diabeteslogger.util.CsvExporter

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

    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }


    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->

        uri?.let {
            CsvExporter.export(
                context = context,
                entries = filteredEntries,
                uri = it
            )
        }
    }

    // ----------------------------
    // GROUP BY DAY (FIXED)
    // ----------------------------
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

    // ----------------------------
    // CHART DATA (AVG PER DAY)
    // ----------------------------
    val chartEntries = remember(groupedByDay) {
        groupedByDay.entries.mapIndexed { index, (_, list) ->
            val avg = list.map { it.value }.average().toFloat()
            Entry(index.toFloat(), avg)
        }
    }

    val dayLabels = remember(groupedByDay) {
        val sdf = SimpleDateFormat("dd/MM", Locale.getDefault())
        groupedByDay.keys.map { sdf.format(Date(it)) }
    }

    Column(modifier = modifier.padding(16.dp)) {

        Text("Glucose Tracker", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            Button(
                onClick = { viewModel.setFilter(FilterType.WEEK) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Week", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = { viewModel.setFilter(FilterType.MONTH) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Month", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = { viewModel.setFilter(FilterType.YEAR) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Year", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = {
                    showDatePicker = true
                    isPickingStart = true
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Range", style = MaterialTheme.typography.labelSmall)
            }
        }

//        Row(
//            horizontalArrangement = Arrangement.spacedBy(8.dp),
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            OutlinedButton(
//                onClick = { showDialog = true },
//                modifier = Modifier.weight(1f)
//            ) {
//                Text("Add glucose")
//            }
//
//            OutlinedButton(onClick = { /* CSV export later */ }) {
//            Text("Export CSV")
//        }
//    }

        Spacer(Modifier.height(16.dp))

        GlucoseChart(entries = chartEntries, labels = dayLabels)

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add glucose")
            }

            OutlinedButton(
                onClick = {
                    exportLauncher.launch("glucose_log.csv")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Export CSV")
            }
        }

        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(5.dp))

//        Button(onClick = { showDialog = true }) {
//            Text("Add glucose")
//        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        ) {

            groupedByDay.forEach { (dayMillis, list) ->

                item {

                    val dateLabel = SimpleDateFormat("dd/MM", Locale.getDefault())
                        .format(Date(dayMillis))

                    Column(modifier = Modifier.padding(vertical = 10.dp)) {

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "📅 $dateLabel",
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        val sorted = list.sortedBy { it.timestamp }

                        val amEntry = sorted.getOrNull(0)
                        val pmEntry = sorted.getOrNull(1)

                        amEntry?.let { entry ->
                            SwipeRow(
                                label = "🌅 Morning",
                                entry = entry,
                                onDelete = { viewModel.deleteEntry(entry) }
                            )
                        }

                        pmEntry?.let { entry ->
                            SwipeRow(
                                label = "🌙 Evening",
                                entry = entry,
                                onDelete = { viewModel.deleteEntry(entry) }
                            )
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
                    onValueChange = { inputText = it.filter { c -> c.isDigit() } },
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

                    val selected = state.selectedDateMillis

                    if (selected != null) {

                        if (isPickingStart) {
                            tempStart = selected
                            isPickingStart = false
                        } else {

                            val start = tempStart ?: selected
                            val end = selected

                            viewModel.setDateRange(
                                minOf(start, end),
                                maxOf(start, end)
                            )

                            showDatePicker = false
                            tempStart = null
                            isPickingStart = true
                        }
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
                    Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entry.timestamp)))
                }
            }
        }
    )
}