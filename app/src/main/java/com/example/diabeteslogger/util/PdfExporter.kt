package com.example.diabeteslogger.util


import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.diabeteslogger.data.local.GlucoseEntry
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    fun export(
        context: Context,
        entries: List<GlucoseEntry>,
        uri: Uri
    ) {
        val document = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val fileDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date())

        var y = 40

        // HEADER
        paint.textSize = 18f
        canvas.drawText("DiabeteMonitor Medical Report", 40f, y.toFloat(), paint)

        y += 30
        paint.textSize = 12f

        canvas.drawText("Generated: $fileDate", 40f, y.toFloat(), paint)
        y += 25

        // SUMMARY
        val values = entries.map { it.value }
        val avg = values.average()
        val max = values.maxOrNull() ?: 0
        val min = values.minOrNull() ?: 0
        val variability = max - min

        canvas.drawText("Average glucose: %.1f mg/dL".format(avg), 40f, y.toFloat(), paint)
        y += 20

        canvas.drawText("Peak glucose: $max mg/dL", 40f, y.toFloat(), paint)
        y += 20

        canvas.drawText("Variability: $variability mg/dL", 40f, y.toFloat(), paint)
        y += 30

        // TABLE HEADER
        paint.textSize = 14f
        canvas.drawText("Date | Value (mg/dL)", 40f, y.toFloat(), paint)
        y += 20

        paint.textSize = 11f

        // LIMIT FOR SAFETY
        entries.take(50).forEach { entry ->
            val line = "${sdf.format(Date(entry.timestamp))} | ${entry.value}"
            canvas.drawText(line, 40f, y.toFloat(), paint)
            y += 18
        }

        document.finishPage(page)

        context.contentResolver.openOutputStream(uri)?.use { output ->
            document.writeTo(output)
        }

        document.close()
    }
}