package com.arcao.geocaching4locus.base.util

import android.content.SharedPreferences

object PreferenceUtil {
    @JvmStatic
    fun getParsedInt(preferences: SharedPreferences, key: String, defaultValue: Int): Int {
        return preferences.getString(key, null)?.toIntOrNull() ?: defaultValue
    }

    @JvmStatic
    fun getParsedDouble(preferences: SharedPreferences, key: String, defaultValue: Double): Double {
        return preferences.getString(key, null)?.toDoubleOrNull() ?: defaultValue
    }

    @JvmStatic
    fun getParsedFloat(preferences: SharedPreferences, key: String, defaultValue: Float): Float {
        return preferences.getString(key, null)?.toFloatOrNull() ?: defaultValue
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedInt(key: String, defaultValue: Int) =
        PreferenceUtil.getParsedInt(this, key, defaultValue)

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedDouble(key: String, defaultValue: Double) =
        PreferenceUtil.getParsedDouble(this, key, defaultValue)

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedFloat(key: String, defaultValue: Float) =
        PreferenceUtil.getParsedFloat(this, key, defaultValue)
