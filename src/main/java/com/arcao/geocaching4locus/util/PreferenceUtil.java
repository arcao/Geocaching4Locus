package com.arcao.geocaching4locus.util;

import android.content.SharedPreferences;
import android.util.Log;

public class PreferenceUtil {
	public static int getParsedInt(SharedPreferences preferences, String key, int defaultValue) {
		String value = preferences.getString(key, null);
		if (value == null)
			return defaultValue;

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			Log.e(PreferenceUtil.class.getSimpleName(), e.getMessage(), e);
			return defaultValue;
		}
	}
}
