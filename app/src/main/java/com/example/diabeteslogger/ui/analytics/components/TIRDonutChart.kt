package com.example.diabeteslogger.ui.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diabeteslogger.ui.analytics.model.TimeInRangeStats

@Composable
fun TIRDonutChart(
    stats: TimeInRangeStats,
    modifier: Modifier = Modifier
) {
    val total = (stats.lowPercent + stats.inRangePercent + stats.highPercent)
        .coerceAtLeast(1f)

    val lowSweep = (stats.lowPercent / total) * 360f
    val inRangeSweep = (stats.inRangePercent / total) * 360f
    val highSweep = (stats.highPercent / total) * 360f

    Box(
        modifier = modifier.size(120.dp), // 👈 smaller diameter
        contentAlignment = Alignment.Center // 👈 keeps everything centered
    ) {

        Canvas(modifier = Modifier.size(120.dp)) {

            val strokeWidth = 20f // 👈 thinner ring for smaller circle

            var startAngle = -90f

            // 🟢 In range
            drawArc(
                color = Color(0xFF4CAF50),
                startAngle = startAngle,
                sweepAngle = inRangeSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = Size(size.width, size.height)
            )
            startAngle += inRangeSweep

            // 🟠 High
            drawArc(
                color = Color(0xFFFF9800),
                startAngle = startAngle,
                sweepAngle = highSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = Size(size.width, size.height)
            )
            startAngle += highSweep

            // 🔴 Low
            drawArc(
                color = Color(0xFFF44336),
                startAngle = startAngle,
                sweepAngle = lowSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt),
                size = Size(size.width, size.height)
            )
        }

        // 📊 CENTER TEXT (perfectly centered now)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${stats.inRangePercent.toInt()}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "TIR",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
    }
}