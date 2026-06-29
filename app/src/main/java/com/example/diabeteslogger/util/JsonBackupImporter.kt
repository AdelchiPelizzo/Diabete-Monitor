package com.example.diabeteslogger.util

import com.example.diabeteslogger.data.local.GlucoseEntry
import org.json.JSONObject

object JsonBackupImporter {

    fun import(jsonString: String): List<GlucoseEntry> {

        val root = JSONObject(jsonString)

        val version = root.getInt("version")
        if (version != 1) {
            throw IllegalArgumentException("Unsupported backup version: $version")
        }

        val entriesArray = root.getJSONArray("entries")

        val list = mutableListOf<GlucoseEntry>()

        for (i in 0 until entriesArray.length()) {

            val obj = entriesArray.getJSONObject(i)

            val entry = GlucoseEntry(
                id = obj.optInt("id", 0),
                value = obj.getInt("glucose"),
                timestamp = obj.getLong("timestamp"),
                session = obj.optString("session", "AM"),
                type = obj.optString("Type", "glucose")
            )

            list.add(entry)
        }

        return list
    }
}