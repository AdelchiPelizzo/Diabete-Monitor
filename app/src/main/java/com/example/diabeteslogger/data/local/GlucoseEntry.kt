package com.example.diabeteslogger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_entries")  // MUST MATCH QUERY
data class GlucoseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val value: Int,
    val timestamp: Long
)