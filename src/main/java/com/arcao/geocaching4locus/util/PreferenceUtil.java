package com.arcao.geocaching4locus.util;

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
			Timber.e(e, e.getMessage());
			return defaultValue;
		}
	}
}
