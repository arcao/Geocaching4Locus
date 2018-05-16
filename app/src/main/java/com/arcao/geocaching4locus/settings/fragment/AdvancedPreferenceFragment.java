package com.arcao.geocaching4locus.settings.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment;
import com.arcao.geocaching4locus.base.util.PreferenceUtil;

import static com.arcao.geocaching4locus.base.constants.AppConstants.*;

public class AdvancedPreferenceFragment extends AbstractPreferenceFragment {
    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference_category_advanced);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);

        if (key == null)
            return;

        switch (key) {
            case IMPERIAL_UNITS:
                boolean imperialUnits = sharedPreferences.getBoolean(IMPERIAL_UNITS, false);
                double distance = PreferenceUtil.getParsedDouble(sharedPreferences, FILTER_DISTANCE,
                        imperialUnits ? DISTANCE_MILES_DEFAULT : DISTANCE_KM_DEFAULT);
                distance = imperialUnits ? distance / MILES_PER_KILOMETER : distance * MILES_PER_KILOMETER;
                sharedPreferences.edit().putString(FILTER_DISTANCE, String.valueOf((float) distance)).apply();
                break;
        }
    }
}
