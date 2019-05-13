package com.arcao.geocaching4locus.base.util

import android.content.SharedPreferences

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedInt(key: String, defValue: Int) =
    getString(key, null)?.toIntOrNull() ?: defValue

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedDouble(key: String, defValue: Double) =
    getString(key, null)?.toDoubleOrNull() ?: defValue

@Suppress("NOTHING_TO_INLINE")
inline fun SharedPreferences.getParsedFloat(key: String, defValue: Float) =
    getString(key, null)?.toFloatOrNull() ?: defValue
