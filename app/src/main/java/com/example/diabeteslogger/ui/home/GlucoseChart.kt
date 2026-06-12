package com.example.diabeteslogger.ui.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun GlucoseChart(
    entries: List<Entry>   // ✅ FIX: now chart receives data, not ViewModel
) {

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                legend.isEnabled = true
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        update = { chart ->

            // If no data, clear chart safely
            if (entries.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val dataSet = LineDataSet(entries, "Glucose (mg/dL)").apply {
                setDrawValues(false)
                lineWidth = 2f
                setDrawCircles(true)
                circleRadius = 4f
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}