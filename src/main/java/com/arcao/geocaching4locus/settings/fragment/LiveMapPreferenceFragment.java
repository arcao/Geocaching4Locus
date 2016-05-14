package com.arcao.geocaching4locus.settings.fragment;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment;

public class LiveMapPreferenceFragment extends AbstractPreferenceFragment {
	private boolean mPremiumMember;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_category_live_map);

		mPremiumMember = App.get(getActivity()).getAccountManager().getRestrictions().isPremiumMember();
	}

	@Override
	protected void preparePreference() {
		super.preparePreference();

		final CheckBoxPreference downloadHintsPreference = findPreference(LIVE_MAP_DOWNLOAD_HINTS, CheckBoxPreference.class);
		downloadHintsPreference.setEnabled(mPremiumMember);

		if (!mPremiumMember) {
			applyPremiumTitleSign(downloadHintsPreference);
		}
	}
}
