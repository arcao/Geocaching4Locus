package com.arcao.geocaching4locus.fragment.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.HtmlUtil;
import org.apache.commons.lang3.StringUtils;
import org.oshkimaadziig.george.androidutils.SpanFormatter;

public abstract class AbstractPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, PrefConstants {
	public static final String VALUE_HTML_FORMAT = "<font color=\"#FF8000\"><b>%s</b></font>";
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
		CharSequence summary = null;
		if (resId != 0)
			summary = getText(resId);

		if (value != null && value.length() > 0)
			return SpanFormatter.format("%s %s", stylizedValue(value), StringUtils.defaultIfEmpty(summary, ""));
		return StringUtils.defaultIfEmpty(summary, "");
	}

	protected CharSequence stylizedValue(CharSequence value) {
		return SpanFormatter.format(HtmlUtil.fromHtml(VALUE_HTML_FORMAT), value);
	}
}
