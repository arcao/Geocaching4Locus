package com.arcao.geocaching4locus.settings.fragment.filter;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment;
import com.arcao.geocaching4locus.settings.widget.ListPreference;

public class TerrainFilterPreferenceFragment extends AbstractPreferenceFragment {
    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_category_filter_terrain);
    }

    @Override
    protected void preparePreference() {
        final ListPreference terrainMinPreference = findPreference(FILTER_TERRAIN_MIN, ListPreference.class);
        final ListPreference terrainMaxPreference = findPreference(FILTER_TERRAIN_MAX, ListPreference.class);

        terrainMinPreference.setSummary(prepareRatingSummary(terrainMinPreference.getValue()));
        terrainMinPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            float min = Float.parseFloat((String) newValue);
            float max = Float.parseFloat(terrainMaxPreference.getValue());

            if (min > max) {
                terrainMaxPreference.setValue((String) newValue);
                terrainMaxPreference.setSummary(prepareRatingSummary((CharSequence) newValue));
            }
            return true;
        });

        terrainMaxPreference.setSummary(prepareRatingSummary(terrainMaxPreference.getValue()));
        terrainMaxPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            float min = Float.parseFloat(terrainMinPreference.getValue());
            float max = Float.parseFloat((String) newValue);

            if (min > max) {
                terrainMinPreference.setValue((String) newValue);
                terrainMinPreference.setSummary(prepareRatingSummary((CharSequence) newValue));
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
            case FILTER_TERRAIN_MIN:
            case FILTER_TERRAIN_MAX:
                final ListPreference difficultyTerrainPreference = findPreference(key, ListPreference.class);
                difficultyTerrainPreference.setSummary(prepareRatingSummary(difficultyTerrainPreference.getEntry()));
                break;
        }
    }

    private CharSequence prepareRatingSummary(CharSequence value) {
        return preparePreferenceSummary(value, 0);
    }
}
