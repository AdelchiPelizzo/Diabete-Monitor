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
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun GlucoseChart(
    entries: List<Entry>,
    labels: List<String>
) {

    AndroidView(
        factory = { context ->

            LineChart(context).apply {

                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false

                setTouchEnabled(true)
                setPinchZoom(true)
                setScaleEnabled(true)

                xAxis.apply {
                    granularity = 1f
                    setDrawGridLines(false)
                    setAvoidFirstLastClipping(true)
                }

                axisLeft.axisMinimum = 50f
                axisLeft.axisMaximum = 650f   // keeps your expanded medical range
            }
        },

        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),

        update = { chart ->

            if (entries.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            // -----------------------------
            // FIXED X AXIS (NO TIMESTAMP)
            // -----------------------------
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return labels.getOrNull(index) ?: ""
                }
            }

            val dataSet = LineDataSet(entries, "Glucose (mg/dL)").apply {

                lineWidth = 3f
                circleRadius = 5f

                setDrawValues(false)
                setDrawFilled(true)

                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}