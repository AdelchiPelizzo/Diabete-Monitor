package com.example.diabeteslogger.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {

    @Insert
    suspend fun insert(entry: GlucoseEntry)

    @Query("SELECT * FROM glucose_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<GlucoseEntry>>
}