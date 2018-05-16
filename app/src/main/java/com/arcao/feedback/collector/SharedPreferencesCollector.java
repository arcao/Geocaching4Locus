package com.arcao.feedback.collector;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Map;

public class SharedPreferencesCollector extends Collector {
    private final Context context;
    private final String sharedPreferenceName;

    public SharedPreferencesCollector(Context context) {
        this(context, null);
    }

    public SharedPreferencesCollector(Context context, String sharedPreferenceName) {
        this.context = context.getApplicationContext();
        this.sharedPreferenceName = sharedPreferenceName;
    }

    @Override
    public String getName() {
        if (sharedPreferenceName == null)
            return "SharedPreferences.default";

        return "SharedPreferences." + sharedPreferenceName;
    }

    @Override
    protected String collect() {
        final SharedPreferences prefs;

        if (sharedPreferenceName == null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        } else {
            prefs = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE);
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
