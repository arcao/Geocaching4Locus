package com.arcao.geocaching4locus.fragment.preference;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.widget.Toolbar;
import android.text.Spanned;
import com.arcao.geocaching4locus.SettingsActivity;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.SpannedFix;

public abstract class AbstractPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, PrefConstants {
	private static final String PARAM_SCREEN = "screen";

	protected SharedPreferences mPrefs;
	protected boolean mHasOptionsMenu = false;

	protected <P extends Preference> P findPreference(String key) {
		return (P)super.findPreference(key);
	}

	protected <P extends Preference> P findPreference(String key, Class<P> clazz) {
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

		if (mHasOptionsMenu) {
			Toolbar toolbar = ((SettingsActivity)getActivity()).getToolBar();
			onCreateOptionsMenu(toolbar.getMenu(), new SupportMenuInflater(getActivity()));
			onPrepareOptionsMenu(toolbar.getMenu());

		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void addPreferencesFromResource(int preferencesResId) {
		super.addPreferencesFromResource(preferencesResId);

		CharSequence title =  getPreferenceScreen().getTitle();
		if (title != null) {
			getActivity().setTitle(title);
		}
	}

	@Override
	public void setHasOptionsMenu(boolean hasMenu) {
		super.setHasOptionsMenu(hasMenu);
		mHasOptionsMenu = hasMenu;
	}

	protected void preparePreference() {
		// empty
	}

	protected Intent createSubScreenIntent(String subScreenKey) {
		return SettingsActivity.createIntent(getActivity(), getClass()).putExtra(PARAM_SCREEN, subScreenKey);
	}

	protected String getSubScreenKey() {
		return getActivity().getIntent().getStringExtra(PARAM_SCREEN);
	}

	protected Spanned preparePreferenceSummary(CharSequence value, int resId) {
		String summary = "";
		if (resId != 0)
			summary = getText(resId).toString();

		if (value != null && value.length() > 0)
			return SpannedFix.fromHtml("<font color=\"#FF8000\"><b>(" + value.toString() + ")</b></font> " + summary);
		return SpannedFix.fromHtml(summary);
	}
}
