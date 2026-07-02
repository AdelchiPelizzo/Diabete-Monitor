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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.Alignment
import java.text.SimpleDateFormat
import java.util.*
import com.example.diabeteslogger.R
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import com.example.diabeteslogger.ui.viewmodel.FilterType
import com.example.diabeteslogger.util.ExportManager
import com.example.diabeteslogger.util.ExportType
import com.github.mikephil.charting.data.Entry
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LogViewModel,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val fileDate = remember {
        SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.getDefault())
            .format(Date())
    }

    val filteredEntries by viewModel.filteredEntries.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()

    // ---------------- EXPORT (CSV) ----------------
    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            ExportManager.export(
                context = context,
                type = ExportType.CSV,
                entries = filteredEntries,
                uri = it
            )
        }
    }

    // ---------------- EXPORT (PDF) ----------------
    val pdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            ExportManager.export(
                context = context,
                type = ExportType.PDF,
                entries = filteredEntries,
                uri = it
            )
        }
    }

    // ---------------- UI STATE ----------------
    var showDatePicker by remember { mutableStateOf(false) }
    var tempStart by remember { mutableStateOf<Long?>(null) }
    var isPickingStart by remember { mutableStateOf(true) }

    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }

    // ---------------- GROUPING ----------------
    val groupedByDay = remember(filteredEntries) {
        filteredEntries
            .sortedByDescending { it.timestamp } // 👈 IMPORTANT: global order first
            .groupBy { entry ->
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

    // ---------------- UI ----------------
    Column(modifier = modifier.padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.glucose_tracker),
                style = MaterialTheme.typography.headlineSmall
            )

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

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
                    FilterType.WEEK -> stringResource(R.string.filter_week)
                    FilterType.MONTH -> stringResource(R.string.filter_month)
                    FilterType.YEAR -> stringResource(R.string.filter_year)
                    FilterType.CUSTOM -> stringResource(R.string.filter_range)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                onClick = { showDialog = true },
                modifier = Modifier.weight(2f)
            ) {
                Text(stringResource(R.string.add_reading))
            }

            OutlinedButton(
                onClick = {
                    csvLauncher.launch("glucose_log_$fileDate.csv")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.export_csv))
            }

            OutlinedButton(
                onClick = {
                    pdfLauncher.launch("medical_report_$fileDate.pdf")
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.export_pdf))
            }
        }

        Spacer(Modifier.height(12.dp))

        Column {

            // ---------------- FIXED HEADER ----------------

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {

                    Text(
                        text = stringResource(R.string.date),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = stringResource(R.string.morning),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall
                    )

                    Text(
                        text = stringResource(R.string.evening),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            HorizontalDivider()

            // ---------------- SCROLLABLE DATA ----------------

            LazyColumn(
                modifier = Modifier.height(350.dp)
            ) {

                items(
                    items = groupedByDay.entries.toList().sortedByDescending { it.key }
                ) { (dayMillis, list) ->

                    val dateLabel = SimpleDateFormat(
                        "dd/MM",
                        Locale.getDefault()
                    ).format(Date(dayMillis))

                    val sorted = list.sortedBy { it.timestamp }

                    val morning = sorted.getOrNull(0)
                    val evening = sorted.getOrNull(1)

                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->

                            if (value != SwipeToDismissBoxValue.Settled) {

                                morning?.let {
                                    viewModel.deleteEntry(it)
                                }

                                evening?.let {
                                    viewModel.deleteEntry(it)
                                }

                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        modifier = Modifier.animateItem(),
                        content = {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // DATE
                                Text(
                                    text = dateLabel,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                )

                                // MORNING
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    morning?.let {

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp)
                                        ) {

                                            Column(
                                                modifier = Modifier.padding(8.dp)
                                            ) {

                                                Text(
                                                    text = it.value.toString()
                                                )

                                                Text(
                                                    text = SimpleDateFormat(
                                                        "HH:mm",
                                                        Locale.getDefault()
                                                    ).format(Date(it.timestamp)),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                // EVENING
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    evening?.let {

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(4.dp)
                                        ) {

                                            Column(
                                                modifier = Modifier.padding(8.dp)
                                            ) {

                                                Text(
                                                    text = it.value.toString()
                                                )

                                                Text(
                                                    text = SimpleDateFormat(
                                                        "HH:mm",
                                                        Locale.getDefault()
                                                    ).format(Date(it.timestamp)),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )

                    HorizontalDivider()
                }
            }
        }
    }

    // ---------------- DIALOG ----------------
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.dialog_add_glucose)) },
            text = {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        if (it.all(Char::isDigit)) inputText = it
                    },
                    label = { Text(stringResource(R.string.glucose_unit)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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

    // ---------------- DATE PICKER ----------------
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
                        SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(entry.timestamp))
                    )
                }
            }
        }
    )
}