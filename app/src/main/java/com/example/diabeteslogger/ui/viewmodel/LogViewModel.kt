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

    private val _filter = MutableStateFlow(FilterType.WEEK)
    val filter = _filter.asStateFlow()

    private val _dateRange = MutableStateFlow<DateRange?>(null)
    val dateRange = _dateRange.asStateFlow()

    fun setFilter(type: FilterType) {
        _filter.value = type
    }

    fun setDateRange(start: Long, end: Long) {
        // FIX: include full end day (23:59:59.999)
        val adjustedEnd = end + TimeUnit.DAYS.toMillis(1) - 1
        _dateRange.value = DateRange(start, adjustedEnd)
    }

    val entries: StateFlow<List<GlucoseEntry>> =
        repository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val filteredEntries: StateFlow<List<GlucoseEntry>> =
        combine(entries, filter, dateRange) { list, f, range ->

            val now = System.currentTimeMillis()

            val base = when (f) {

                FilterType.WEEK ->
                    list.filter { it.timestamp >= now - 7L * 24 * 60 * 60 * 1000 }

                FilterType.MONTH ->
                    list.filter { it.timestamp >= now - 30L * 24 * 60 * 60 * 1000 }

                FilterType.YEAR ->
                    list.filter { it.timestamp >= now - 365L * 24 * 60 * 60 * 1000 }

                FilterType.CUSTOM -> {
                    range?.let { r ->
                        list.filter { it.timestamp in r.start..r.end }
                    } ?: list
                }
            }

            base.sortedBy { it.timestamp }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addEntry(value: Int) {
        viewModelScope.launch {
            repository.insert(value)
        }
    }

    fun deleteEntry(entry: GlucoseEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }

    fun getDailyGrouped(): Map<String, Pair<GlucoseEntry?, GlucoseEntry?>> {
        val sdf = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())

        return filteredEntries.value
            .groupBy { sdf.format(java.util.Date(it.timestamp)) }
            .mapValues { (_, list) ->
                val sorted = list.sortedBy { it.timestamp }
                sorted.getOrNull(0) to sorted.getOrNull(1)
            }
    }
}