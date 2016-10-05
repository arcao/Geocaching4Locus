package com.arcao.geocaching4locus.base.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.util.HtmlUtil;
import com.arcao.geocaching4locus.base.util.ResourcesUtil;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, PrefConstants {
	protected SharedPreferences mPrefs;

	@SuppressWarnings("unchecked")
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
		if (!getResources().getBoolean(R.bool.preferences_prefer_dual_pane))
			getActivity().setTitle(getPreferenceScreen().getTitle());
	}
	protected CharSequence preparePreferenceSummary(CharSequence value, int resId) {
		String summary = "";
		if (resId != 0)
			summary = ResourcesUtil.getHtmlString(getActivity(), resId);

		if (value != null && value.length() > 0)
			return HtmlUtil.fromHtml("<font color=\"#FF8000\"><b>(" + value.toString() + ")</b></font> " + StringUtils.defaultString(summary));
		return HtmlUtil.fromHtml(StringUtils.defaultString(summary));
	}

	protected void applyPremiumTitleSign(Preference preference) {
		preference.setTitle(String.format("%s %s", preference.getTitle(), AppConstants.PREMIUM_CHARACTER));
	}
}
