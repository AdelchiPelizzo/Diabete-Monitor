package com.example.diabeteslogger.data.repository

import com.example.diabeteslogger.data.local.GlucoseDao
import com.example.diabeteslogger.data.local.GlucoseEntry
import kotlinx.coroutines.flow.Flow

class GlucoseRepository(
    private val dao: GlucoseDao
) {
    fun getAll(): Flow<List<GlucoseEntry>> = dao.getAll()

    suspend fun insert(value: Int) {
        dao.insert(
            GlucoseEntry(
                value = value,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}