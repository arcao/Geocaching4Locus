package com.arcao.geocaching4locus.base.util

import android.content.SharedPreferences

object PreferenceUtil {
    @Deprecated("Use inline function.", ReplaceWith("preferences.getParsedInt(key, defValue)"))
    @JvmStatic
    fun getParsedInt(preferences: SharedPreferences, key: String, defValue: Int): Int {
        return preferences.getParsedInt(key, defValue)
    }

    @Deprecated("Use inline function.", ReplaceWith("preferences.getParsedDouble(key, defValue)"))
    @JvmStatic
    fun getParsedDouble(preferences: SharedPreferences, key: String, defValue: Double): Double {
        return preferences.getParsedDouble(key, defValue)
    }

    @Deprecated("Use inline function.", ReplaceWith("preferences.getParsedFloat(key, defValue)"))
    @JvmStatic
    fun getParsedFloat(preferences: SharedPreferences, key: String, defValue: Float): Float {
        return preferences.getParsedFloat(key, defValue)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedInt(key: String, defValue: Int) =
    getString(key, null)?.toIntOrNull() ?: defValue

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedDouble(key: String, defValue: Double) =
    getString(key, null)?.toDoubleOrNull() ?: defValue

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedFloat(key: String, defValue: Float) =
    getString(key, null)?.toFloatOrNull() ?: defValue
