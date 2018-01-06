package com.arcao.geocaching4locus.base.util;

import android.content.SharedPreferences;

import timber.log.Timber;

public class PreferenceUtil {
    public static int getParsedInt(SharedPreferences preferences, String key, int defaultValue) {
        String value = preferences.getString(key, null);
        if (value == null)
            return defaultValue;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Timber.e(e);
            return defaultValue;
        }
    }

    public static double getParsedDouble(SharedPreferences preferences, String key, double defaultValue) {
        String value = preferences.getString(key, null);
        if (value == null)
            return defaultValue;

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Timber.e(e);
            return defaultValue;
        }
    }

    public static float getParsedFloat(SharedPreferences preferences, String key, float defaultValue) {
        String value = preferences.getString(key, null);
        if (value == null)
            return defaultValue;

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            Timber.e(e);
            return defaultValue;
        }
    }

}
