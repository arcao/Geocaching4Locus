package com.arcao.geocaching4locus.settings.fragment.filter;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment;
import com.arcao.geocaching4locus.settings.widget.ListPreference;

public class DifficultyFilterPreferenceFragment extends AbstractPreferenceFragment {
    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_category_filter_difficulty);
    }

    @Override
    protected void preparePreference() {
        final ListPreference difficultyMinPreference = findPreference(FILTER_DIFFICULTY_MIN, ListPreference.class);
        final ListPreference difficultyMaxPreference = findPreference(FILTER_DIFFICULTY_MAX, ListPreference.class);

        difficultyMinPreference.setSummary(prepareRatingSummary(difficultyMinPreference.getValue()));
        difficultyMinPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            float min = Float.parseFloat((String) newValue);
            float max = Float.parseFloat(difficultyMaxPreference.getValue());

            if (min > max) {
                difficultyMaxPreference.setValue((String) newValue);
                difficultyMaxPreference.setSummary(prepareRatingSummary((CharSequence) newValue));
            }
            return true;
        });

        difficultyMaxPreference.setSummary(prepareRatingSummary(difficultyMaxPreference.getValue()));
        difficultyMaxPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            float min = Float.parseFloat(difficultyMinPreference.getValue());
            float max = Float.parseFloat((String) newValue);

            if (min > max) {
                difficultyMinPreference.setValue((String) newValue);
                difficultyMinPreference.setSummary(prepareRatingSummary((CharSequence) newValue));
            }
            return true;
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (key == null)
            return;

        switch (key) {
            case FILTER_DIFFICULTY_MIN:
            case FILTER_DIFFICULTY_MAX:
                final ListPreference difficultyTerrainPreference = findPreference(key, ListPreference.class);
                difficultyTerrainPreference.setSummary(prepareRatingSummary(difficultyTerrainPreference.getEntry()));
                break;
        }
    }

    CharSequence prepareRatingSummary(CharSequence value) {
        return preparePreferenceSummary(value, 0);
    }
}
