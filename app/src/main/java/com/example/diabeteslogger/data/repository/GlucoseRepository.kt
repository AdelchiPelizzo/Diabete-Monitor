package com.example.diabeteslogger.data.repository

import com.example.diabeteslogger.data.local.GlucoseDao
import com.example.diabeteslogger.data.local.GlucoseEntry
import kotlinx.coroutines.flow.Flow

class GlucoseRepository(
    private val dao: GlucoseDao
) {
    fun getAll(): Flow<List<GlucoseEntry>> = dao.getAll()

    suspend fun insert(value: Int) {

        val now = System.currentTimeMillis()

        val hour = java.util.Calendar.getInstance().apply {
            timeInMillis = now
        }.get(java.util.Calendar.HOUR_OF_DAY)

        val session = if (hour < 12) "AM" else "PM"

        dao.insert(
            GlucoseEntry(
                value = value,
                timestamp = now,
                session = session
            )
        )
    }

    suspend fun delete(entry: GlucoseEntry) {
        dao.delete(entry)
    }
}