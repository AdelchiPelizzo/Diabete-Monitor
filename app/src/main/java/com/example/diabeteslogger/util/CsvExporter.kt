package com.example.diabeteslogger.util

import android.content.Context
import android.net.Uri
import com.example.diabeteslogger.data.local.GlucoseEntry
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    fun export(
        context: Context,
        entries: List<GlucoseEntry>,
        uri: Uri
    ) {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        context.contentResolver.openOutputStream(uri)?.use { output ->
            val writer = OutputStreamWriter(output)

            // Header
            writer.append("id,value,timestamp,date\n")

            // Rows
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