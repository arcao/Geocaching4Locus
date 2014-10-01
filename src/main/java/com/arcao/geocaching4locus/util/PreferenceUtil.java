package com.arcao.geocaching4locus.util;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Arcao on 1. 10. 2014.
 */
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
