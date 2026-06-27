package com.example.diabeteslogger.util

import android.content.Context
import android.net.Uri
import com.example.diabeteslogger.data.local.GlucoseEntry
import com.example.diabeteslogger.util.CsvExporter

enum class ExportType {
    CSV,
    PDF,
    JSON_BACKUP
}
object ExportManager {

    fun export(
        context: Context,
        type: ExportType,
        entries: List<GlucoseEntry>,
        uri: Uri
    ) {
        when (type) {
            ExportType.CSV -> CsvExporter.export(context, entries, uri)
            ExportType.PDF -> PdfExporter.export(context, entries, uri)
            ExportType.JSON_BACKUP -> JsonBackupExporter.export(context, entries, uri)
        }
    }
}