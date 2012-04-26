package org.acra;

import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesCollector {

	public static String collect(Context context) {
		StringBuilder result = new StringBuilder();
		Map<String, SharedPreferences> shrdPrefs = new TreeMap<String, SharedPreferences>();
		shrdPrefs.put("default", PreferenceManager.getDefaultSharedPreferences(context));
		String[] shrdPrefsIds = ACRA.getConfig().additionalSharedPreferences();
		if (shrdPrefsIds != null) {
			for (String shrdPrefId : shrdPrefsIds) {
				shrdPrefs.put(shrdPrefId, context.getSharedPreferences(shrdPrefId, Context.MODE_PRIVATE));
			}
		}

		SharedPreferences prefs = null;
		for (String prefsId : shrdPrefs.keySet()) {
			prefs = shrdPrefs.get(prefsId);
			result.append(prefsId).append("\n");
			if (prefs != null) {
				result.append(collectPrefsForSharedPrefs(prefs));
			} else {
				result.append("null\n");
			}
			result.append("\n");
		}

		return result.toString();
	}

	public static String collectPrefsForSharedPrefs(SharedPreferences prefs) {

		StringBuilder result = new StringBuilder();
		String[] lOfPrefsToOmit = ACRA.getConfig().omitSharedPrefs();

		Map<String, ?> kv = prefs.getAll();
		if (kv != null && kv.size() > 0) {
			for (String key : kv.keySet()) {
				boolean skip = false;
				if (lOfPrefsToOmit != null) {
					for (String omitKeys : lOfPrefsToOmit) {
						if (omitKeys.equals(key)) {
							skip = true;
							break;
						}
					}
				}
				if (skip) {
					result.append(key).append("=").append("******").append("\n");
				} else {
					result.append(key).append("=").append(kv.get(key).toString()).append("\n");
				}
			}
		} else {
			result.append("empty\n");
		}
		return result.toString();
	}
}
