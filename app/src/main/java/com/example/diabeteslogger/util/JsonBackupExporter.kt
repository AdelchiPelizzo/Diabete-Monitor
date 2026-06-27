package com.example.diabeteslogger.util

import android.content.Context
import android.net.Uri
import com.example.diabeteslogger.data.local.GlucoseEntry
import org.json.JSONArray
import org.json.JSONObject

object JsonBackupExporter {

    fun export(
        context: Context,
        entries: List<GlucoseEntry>,
        uri: Uri
    ) {
        val jsonArray = JSONArray()

        entries.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("Type", entry.type)
                put("glucose", entry.value)
                put("timestamp", entry.timestamp)
            }
            jsonArray.put(obj)
        }

        val root = JSONObject().apply {
            put("version", 1)
            put("entries", jsonArray)
        }

        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(root.toString(2).toByteArray())
        }
    }
}