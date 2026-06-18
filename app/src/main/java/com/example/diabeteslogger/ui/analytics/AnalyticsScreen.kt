package com.example.diabeteslogger.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diabeteslogger.ui.viewmodel.LogViewModel
import java.util.*

@Composable
fun AnalyticsScreen(
    viewModel: LogViewModel
) {

    val entries by viewModel.filteredEntries.collectAsState()

    val groupedByDay = remember(entries) {
        entries.groupBy {
            Calendar.getInstance().apply {
                timeInMillis = it.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }

    val values = remember(entries) { entries.map { it.value } }

    val avg = remember(values) { values.average() }
    val max = remember(values) { values.maxOrNull() ?: 0 }
    val min = remember(values) { values.minOrNull() ?: 0 }

    val variability = remember(values) {
        if (values.isNotEmpty()) max - min else 0
    }

    val amPmDiff = remember(groupedByDay) {
        groupedByDay.values.map { list ->
            val sorted = list.sortedBy { it.timestamp }
            val am = sorted.getOrNull(0)?.value ?: 0
            val pm = sorted.getOrNull(1)?.value ?: 0
            pm - am
        }.average()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("Analytics", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        InsightCardView(
            InsightCard(
                title = "📊 Average glucose",
                value = "${"%.1f".format(avg)} mg/dL",
                subtitle = "Overall average",
                severity = InsightSeverity.LOW
            )
        )

        InsightCardView(
            InsightCard(
                title = "🔺 Peak",
                value = "$max mg/dL",
                subtitle = "Highest recorded",
                severity = InsightSeverity.MEDIUM
            )
        )

        InsightCardView(
            InsightCard(
                title = "⚖️ Variability",
                value = "$variability mg/dL",
                subtitle = "Spread of values",
                severity = InsightSeverity.MEDIUM
            )
        )

        InsightCardView(
            InsightCard(
                title = "🌙 AM vs PM",
                value = "${"%.1f".format(amPmDiff)} mg/dL",
                subtitle = "Evening vs morning trend",
                severity = InsightSeverity.LOW
            )
        )
    }
}