package com.arcao.geocaching4locus.fragment.preference;

import android.os.Bundle;
import com.arcao.geocaching4locus.R;

public class AdvancedPreferenceFragment extends AbstractPreferenceFragment {
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_category_advanced);
	}
}
