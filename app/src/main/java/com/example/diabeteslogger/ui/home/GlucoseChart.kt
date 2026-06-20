package com.example.diabeteslogger.ui.home

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.diabeteslogger.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

@Composable
fun GlucoseChart(
    morningEntries: List<Entry>,
    eveningEntries: List<Entry>,
    averageEntries: List<Entry>,
    labels: List<String>
) {

    val morningLabel =
        stringResource(R.string.chart_morning)

    val eveningLabel =
        stringResource(R.string.chart_evening)

    val averageLabel =
        stringResource(R.string.chart_average)

    AndroidView(
        factory = { context ->

            LineChart(context).apply {

                description.isEnabled = false
                legend.isEnabled = true

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
                axisLeft.axisMaximum = 650f
            }
        },

        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),

        update = { chart ->

            if (
                morningEntries.isEmpty() &&
                eveningEntries.isEmpty() &&
                averageEntries.isEmpty()
            ) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            chart.xAxis.valueFormatter =
                object : ValueFormatter() {

                    override fun getFormattedValue(
                        value: Float
                    ): String {
                        return labels.getOrNull(
                            value.toInt()
                        ) ?: ""
                    }
                }

            val morningSet =
                LineDataSet(
                    morningEntries,
                    morningLabel
                ).apply {

                    color = Color.BLUE
                    setCircleColor(Color.BLUE)

                    lineWidth = 3f
                    circleRadius = 5f

                    setDrawValues(false)

                    mode =
                        LineDataSet.Mode.CUBIC_BEZIER
                }

            val eveningSet =
                LineDataSet(
                    eveningEntries,
                    eveningLabel
                ).apply {

                    color = Color.RED
                    setCircleColor(Color.RED)

                    lineWidth = 3f
                    circleRadius = 5f

                    setDrawValues(false)

                    mode =
                        LineDataSet.Mode.CUBIC_BEZIER
                }

            val avgSet =
                LineDataSet(
                    averageEntries,
                    averageLabel
                ).apply {

                    color = Color.DKGRAY
                    setCircleColor(Color.DKGRAY)

                    lineWidth = 2f
                    circleRadius = 4f

                    setDrawValues(false)

                    enableDashedLine(
                        10f,
                        6f,
                        0f
                    )

                    mode =
                        LineDataSet.Mode.CUBIC_BEZIER
                }

            chart.data =
                LineData(
                    morningSet,
                    eveningSet,
                    avgSet
                )

            chart.marker =
                GlucoseMarkerView(
                    chart.context,
                    labels,
                    morningEntries,
                    eveningEntries,
                    averageEntries
                )

            chart.invalidate()
        }
    )
}