package com.arcao.geocaching4locus.base.util

import android.content.SharedPreferences

fun SharedPreferences.getParsedInt(key: String, defValue: Int) =
    getString(key, null)?.toIntOrNull() ?: defValue

fun SharedPreferences.getParsedDouble(key: String, defValue: Double) =
    getString(key, null)?.toDoubleOrNull() ?: defValue

fun SharedPreferences.getParsedFloat(key: String, defValue: Float) =
    getString(key, null)?.toFloatOrNull() ?: defValue
