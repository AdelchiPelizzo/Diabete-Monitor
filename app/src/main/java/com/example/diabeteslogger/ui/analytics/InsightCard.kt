package com.example.diabeteslogger.ui.analytics

enum class InsightSeverity {
    LOW,
    MEDIUM,
    HIGH
}

data class InsightCard(
    val title: String,
    val value: String,
    val subtitle: String,
    val severity: InsightSeverity
)