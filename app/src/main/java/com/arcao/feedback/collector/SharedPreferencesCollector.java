package com.arcao.feedback.collector;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

public class SharedPreferencesCollector extends Collector {
	private final Context mContext;
	private final String mSharedPreferenceName;

	public SharedPreferencesCollector(Context context) {
		this(context, null);
	}

	public SharedPreferencesCollector(Context context, String sharedPreferenceName) {
		mContext = context.getApplicationContext();
		mSharedPreferenceName = sharedPreferenceName;
	}

	@Override
	public String getName() {
		if (mSharedPreferenceName == null)
			return "SharedPreferences.default";

		return "SharedPreferences." + mSharedPreferenceName;
	}

	@Override
	protected String collect() {
		final SharedPreferences prefs;

		if (mSharedPreferenceName == null) {
			prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		}	else {
			prefs = mContext.getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
		}

		final Map<String, ?> prefEntries = prefs.getAll();

		// In case it's empty return empty text
		if (prefEntries.isEmpty()) {
			return "Empty";
		}

		StringBuilder result = new StringBuilder();
		// Add all preferences
		for (final String key : prefEntries.keySet()) {
			final Object prefValue = prefEntries.get(key);
			result.append(key).append('=');
			result.append(prefValue == null ? "null" : prefValue.toString());
			result.append("\n");
		}
		return result.toString();
	}
}
