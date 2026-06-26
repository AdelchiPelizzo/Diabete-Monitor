package com.example.diabeteslogger.util

import com.example.diabeteslogger.data.local.GlucoseEntry
import com.example.diabeteslogger.ui.analytics.model.TimeInRangeStats

class AnalyticsCalculator {
}

fun calculateTIR(entries: List<GlucoseEntry>): TimeInRangeStats {

    if (entries.isEmpty()) {
        return TimeInRangeStats(0f, 0f, 0f)
    }

    val total = entries.size

    val low = entries.count { it.value < 70 }
    val inRange = entries.count { it.value in 70..180 }
    val high = entries.count { it.value > 180 }

    return TimeInRangeStats(
        lowPercent = low * 100f / total,
        inRangePercent = inRange * 100f / total,
        highPercent = high * 100f / total
    )
}