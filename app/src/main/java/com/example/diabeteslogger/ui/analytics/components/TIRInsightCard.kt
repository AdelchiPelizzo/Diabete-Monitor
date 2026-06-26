package com.example.diabeteslogger.ui.analytics.components

import androidx.compose.runtime.Composable
import com.example.diabeteslogger.ui.analytics.InsightCard
import com.example.diabeteslogger.ui.analytics.InsightCardView
import com.example.diabeteslogger.ui.analytics.InsightSeverity
import com.example.diabeteslogger.ui.analytics.model.TimeInRangeStats

class TIRInsightCard {
}

@Composable
fun TIRInsightCard(stats: TimeInRangeStats) {

    InsightCardView(
        InsightCard(
            title = "⏱ Time in Range",
            value = "${"%.0f".format(stats.inRangePercent)}%",
            subtitle = "70–180 mg/dL",
            severity = when {
                stats.inRangePercent >= 70f -> InsightSeverity.LOW
                stats.inRangePercent >= 50f -> InsightSeverity.MEDIUM
                else -> InsightSeverity.HIGH
            }
        )
    )
}