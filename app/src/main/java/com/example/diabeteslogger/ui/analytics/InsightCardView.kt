package com.example.diabeteslogger.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InsightCardView(card: InsightCard) {

    val color = when (card.severity) {
        InsightSeverity.LOW -> MaterialTheme.colorScheme.primary
        InsightSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
        InsightSeverity.HIGH -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(card.title, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(4.dp))

            Text(card.value, style = MaterialTheme.typography.headlineSmall)

            Spacer(Modifier.height(4.dp))

            Text(card.subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}