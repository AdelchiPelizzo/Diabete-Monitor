package com.example.diabeteslogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabeteslogger.data.local.GlucoseEntry
import com.example.diabeteslogger.data.repository.GlucoseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class FilterType {
    WEEK,
    MONTH,
    YEAR,
    CUSTOM
}

data class DateRange(
    val start: Long,
    val end: Long
)

class LogViewModel(
    private val repository: GlucoseRepository
) : ViewModel() {

    // -----------------------------
    // FILTER STATE (preset vs custom)
    // -----------------------------
    private val _filter = MutableStateFlow(FilterType.WEEK)
    val filter = _filter.asStateFlow()

    // -----------------------------
    // CUSTOM RANGE STATE
    // -----------------------------
    private val _dateRange = MutableStateFlow<DateRange?>(null)
    val dateRange = _dateRange.asStateFlow()

    // -----------------------------
    // SET PRESET FILTER
    // -----------------------------
    fun setFilter(type: FilterType) {
        _filter.value = type

        // IMPORTANT: clear range when switching presets
        if (type != FilterType.CUSTOM) {
            _dateRange.value = null
        }
        android.util.Log.d("FILTER_DEBUG", "setFilter -> $type")
    }

    // -----------------------------
    // SET CUSTOM RANGE
    // -----------------------------
    fun setDateRange(start: Long, end: Long) {

        val adjustedEnd =
            end + TimeUnit.DAYS.toMillis(1) - 1

        _dateRange.value = DateRange(start, adjustedEnd)
        _filter.value = FilterType.CUSTOM

        android.util.Log.d("FILTER_DEBUG", "CUSTOM RANGE SET: $start - $adjustedEnd")
    }

    // -----------------------------
    // DATABASE STREAM
    // -----------------------------
    val entries: StateFlow<List<GlucoseEntry>> =
        repository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // -----------------------------
    // FILTERED DATA (MAIN SOURCE)
    // -----------------------------
    val filteredEntries: StateFlow<List<GlucoseEntry>> =
        combine(entries, filter, dateRange) { list, f, range ->

            val now = System.currentTimeMillis()

            val filtered = when (f) {

                FilterType.WEEK ->
                    list.filter {
                        it.timestamp >= now - 7L * 24 * 60 * 60 * 1000
                    }

                FilterType.MONTH ->
                    list.filter {
                        it.timestamp >= now - 30L * 24 * 60 * 60 * 1000
                    }

                FilterType.YEAR ->
                    list.filter {
                        it.timestamp >= now - 365L * 24 * 60 * 60 * 1000
                    }

                FilterType.CUSTOM -> {
                    val r = range
                    if (r != null) {
                        list.filter {
                            it.timestamp in r.start..r.end
                        }
                    } else {
                        emptyList()
                    }
                }
            }

            filtered.sortedBy { it.timestamp }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // -----------------------------
    // INSERT
    // -----------------------------
    fun addEntry(value: Int) {
        viewModelScope.launch {
            repository.insert(value)
        }
    }

    // -----------------------------
    // DELETE
    // -----------------------------
    fun deleteEntry(entry: GlucoseEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    // -----------------------------
    // GROUPED VIEW (UI ONLY)
    // -----------------------------
    fun getDailyGrouped(): Map<String, Pair<GlucoseEntry?, GlucoseEntry?>> {

        val sdf =
            java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

        return filteredEntries.value
            .groupBy { sdf.format(java.util.Date(it.timestamp)) }
            .mapValues { (_, list) ->

                val sorted = list.sortedBy { it.timestamp }

                val am = sorted.getOrNull(0)
                val pm = sorted.getOrNull(1)

                am to pm
            }
    }
}