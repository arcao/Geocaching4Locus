package com.arcao.feedback.collector

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPreferencesCollector @JvmOverloads constructor(private val context: Context, private val sharedPreferenceName: String? = null) : Collector() {
    override val name: String
        get() = if (sharedPreferenceName == null) "SharedPreferences.default" else "SharedPreferences.$sharedPreferenceName"

    override suspend fun collect(): String {
        val prefs: SharedPreferences = if (sharedPreferenceName == null) {
            PreferenceManager.getDefaultSharedPreferences(context)
        } else {
            context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)
        }

        val entries = prefs.all

        // In case it's empty return empty text
        if (entries.isEmpty()) {
            return "Empty"
        }

        val result = StringBuilder()
        // Add all preferences
        entries.forEach { (key, value) ->
            result.append(key)
                    .append('=')
                    .append(value.toString())
                    .append("\n")
        }
        return result.toString()
    }
}
