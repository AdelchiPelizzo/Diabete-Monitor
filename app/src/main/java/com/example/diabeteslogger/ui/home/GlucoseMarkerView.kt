package com.example.diabeteslogger.ui.home

import android.content.Context
import android.widget.TextView
import com.example.diabeteslogger.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class GlucoseMarkerView(
    context: Context,
    private val labels: List<String>,
    private val morning: List<Entry>,
    private val evening: List<Entry>,
    private val average: List<Entry>
) : MarkerView(context, R.layout.marker_view) {

    private val textView: TextView =
        findViewById(R.id.markerText)

    override fun refreshContent(
        e: Entry?,
        highlight: Highlight?
    ) {

        val index =
            e?.x?.toInt() ?: 0

        val date =
            labels.getOrNull(index)
                ?: context.getString(R.string.marker_unknown)

        val morningLabel =
            context.getString(R.string.marker_morning)

        val eveningLabel =
            context.getString(R.string.marker_evening)

        val averageLabel =
            context.getString(R.string.marker_average)

        val m =
            morning.getOrNull(index)?.y

        val ev =
            evening.getOrNull(index)?.y

        val avg =
            average.getOrNull(index)?.y

        textView.text =
            """
            📅 $date
            
            🌅 $morningLabel: ${m?.toInt() ?: "-"}
            🌙 $eveningLabel: ${ev?.toInt() ?: "-"}
            ⚫ $averageLabel: ${avg?.toInt() ?: "-"}
            """.trimIndent()

        super.refreshContent(
            e,
            highlight
        )
    }

    override fun getOffset(): MPPointF {
        return MPPointF(
            -(width / 2f),
            -height.toFloat()
        )
    }
}