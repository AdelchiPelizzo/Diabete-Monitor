package com.example.diabeteslogger.util

import android.content.Context
import android.net.Uri
import com.example.diabeteslogger.data.local.GlucoseEntry
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import com.example.diabeteslogger.R

object CsvExporter {

    fun export(
        context: Context,
        entries: List<GlucoseEntry>,
        uri: Uri
    ) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        context.contentResolver.openOutputStream(uri)?.use { output ->
            val writer = OutputStreamWriter(output)

            // HEADER (localized)
            writer.append(
                "${context.getString(R.string.csv_id)}," +
                        "${context.getString(R.string.csv_value)}," +
                        "${context.getString(R.string.csv_timestamp)}," +
                        "${context.getString(R.string.csv_date)}\n"
            )

            // ROWS
            entries.forEach { entry ->
                writer.append("${entry.id},")
                writer.append("${entry.value},")
                writer.append("${entry.timestamp},")
                writer.append("${sdf.format(Date(entry.timestamp))}\n")
            }

            writer.flush()
        }
    }
}