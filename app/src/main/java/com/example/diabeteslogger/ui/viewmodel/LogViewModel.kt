package com.example.diabeteslogger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diabeteslogger.data.local.GlucoseEntry
import com.example.diabeteslogger.data.repository.GlucoseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FilterType {
    TODAY,
    WEEK
}

class LogViewModel(
    private val repository: GlucoseRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(FilterType.TODAY)
    val filter = _filter.asStateFlow()

    val entries: StateFlow<List<GlucoseEntry>> =
        repository.getAll()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val filteredEntries: StateFlow<List<GlucoseEntry>> =
        combine(entries, filter) { list, f ->
            val now = System.currentTimeMillis()

            when (f) {
                FilterType.TODAY -> {
                    val start = now - 24 * 60 * 60 * 1000
                    list.filter { it.timestamp >= start }
                }

                FilterType.WEEK -> {
                    val week = now - 7 * 24 * 60 * 60 * 1000
                    list.filter { it.timestamp >= week }
                }
            }.sortedBy { it.timestamp }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun setFilter(type: FilterType) {
        _filter.value = type
    }

    fun addEntry(value: Int) {
        viewModelScope.launch {
            repository.insert(value)
        }
    }
}