package com.example.diabeteslogger.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {

    @Insert
    suspend fun insert(entry: GlucoseEntry)

    @Query("SELECT * FROM glucose_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<GlucoseEntry>>

    @Delete
    suspend fun delete(entry: GlucoseEntry)

    // ✅ ADD THIS
    @Query("DELETE FROM glucose_entries")
    suspend fun deleteAll()
}