package com.arcao.geocaching4locus.fragment.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.SpannedFix;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, PrefConstants {
	protected SharedPreferences mPrefs;

	protected <P extends Preference> P findPreference(CharSequence key, Class<P> clazz) {
		return (P)super.findPreference(key);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// empty
	}

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		preparePreference();
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	protected void preparePreference() {
		// empty
	}
	protected CharSequence preparePreferenceSummary(CharSequence value, int resId) {
		String summary = "";
		if (resId != 0)
			summary = getText(resId).toString();

		if (value != null && value.length() > 0)
			return SpannedFix.fromHtml("<font color=\"#FF8000\"><b>(" + value.toString() + ")</b></font> " + StringUtils.defaultString(summary));
		return SpannedFix.fromHtml(StringUtils.defaultString(summary));
	}
}
