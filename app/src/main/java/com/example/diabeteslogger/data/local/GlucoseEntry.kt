package com.example.diabeteslogger.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_entries")
data class GlucoseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val value: Int,
    val timestamp: Long,

    // NEW FIELD
    val session: String // "AM" or "PM"
)