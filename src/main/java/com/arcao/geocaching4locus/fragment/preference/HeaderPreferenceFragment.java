package com.arcao.geocaching4locus.fragment.preference;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.SettingsActivity;
import com.arcao.preference.FragmentPreference;

public class HeaderPreferenceFragment extends AbstractPreferenceFragment {
	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_header);

		applyIntentOnPreferences();
	}

	private void applyIntentOnPreferences() {
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			Preference preference = getPreferenceScreen().getPreference(i);

			if (!(preference instanceof FragmentPreference))
				continue;

			final String fragment = ((FragmentPreference)preference).getFragmentName();
			if (fragment == null)
				continue;

			preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(getActivity(), SettingsActivity.class);
					intent.putExtra(SettingsActivity.PARAM_FRAGMENT, fragment);
					getActivity().startActivity(intent);
					return true;
				}
			});
		}
	}
}
