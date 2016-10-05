package com.arcao.geocaching4locus.settings.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment;
import com.arcao.geocaching4locus.base.constants.PrefConstants;

public class FilterPreferenceFragment extends AbstractPreferenceFragment {
	public static final String TEXT_VALUE_SEPARATOR = ", ";

	private boolean mPremiumMember;
	private boolean mImperialUnits;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_category_filter);

		mPremiumMember = App.get(getActivity()).getAccountManager().isPremium();
		mImperialUnits = mPrefs.getBoolean(PrefConstants.IMPERIAL_UNITS, false);
	}

	@Override
	protected void preparePreference() {
		prepareCacheTypePreference();
		prepareContainerTypePreference();
		prepareDifficultyPreference();
		prepareTerrainPreference();
		prepareDistancePreference();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);

		if (key == null)
			return;

		switch (key) {
			case FILTER_DISTANCE:
				final EditTextPreference distancePreference = findPreference(key, EditTextPreference.class);
				if (mImperialUnits) {
					distancePreference.setSummary(preparePreferenceSummary(distancePreference.getText() + UNIT_MILES, R.string.pref_distance_summary_miles));
				} else {
					distancePreference.setSummary(preparePreferenceSummary(distancePreference.getText() + UNIT_KM, R.string.pref_distance_summary_km));
				}
				break;
		}
	}

	private void prepareCacheTypePreference() {
		final Preference cacheTypeFilterScreen = findPreference(FILTER_CACHE_TYPE, Preference.class);
		cacheTypeFilterScreen.setEnabled(mPremiumMember);
		cacheTypeFilterScreen.setSummary(mPremiumMember ? prepareCacheTypeSummary() : prepareCacheTypeSummaryBasicMember());
		if (!mPremiumMember) {
			applyPremiumTitleSign(cacheTypeFilterScreen);
		}
	}

	private void prepareContainerTypePreference() {
		final Preference containerTypeFilterScreen = findPreference(FILTER_CONTAINER_TYPE, Preference.class);
		containerTypeFilterScreen.setEnabled(mPremiumMember);
		containerTypeFilterScreen.setSummary(
				mPremiumMember ? prepareContainerTypeSummary() : prepareContainerTypeSummaryBasicMember());
		if (!mPremiumMember) {
			applyPremiumTitleSign(containerTypeFilterScreen);
		}
	}

	private void prepareDifficultyPreference() {
		final Preference difficultyPreference = findPreference(FILTER_DIFFICULTY, Preference.class);
		difficultyPreference.setEnabled(mPremiumMember);

		String difficultyMin = "1";
		String difficultyMax = "5";

		if (mPremiumMember) {
			difficultyMin = mPrefs.getString(PrefConstants.FILTER_DIFFICULTY_MIN, difficultyMin);
			difficultyMax = mPrefs.getString(PrefConstants.FILTER_DIFFICULTY_MAX, difficultyMax);
		} else {
			applyPremiumTitleSign(difficultyPreference);
		}

		difficultyPreference.setSummary(prepareRatingSummary(difficultyMin, difficultyMax));
	}

	private void prepareTerrainPreference() {
		final Preference terrainPreference = findPreference(FILTER_TERRAIN, Preference.class);
		terrainPreference.setEnabled(mPremiumMember);

		String terrainMin = "1";
		String terrainMax = "5";

		if (mPremiumMember) {
			terrainMin = mPrefs.getString(PrefConstants.FILTER_TERRAIN_MIN, terrainMin);
			terrainMax = mPrefs.getString(PrefConstants.FILTER_TERRAIN_MAX, terrainMax);
		} else {
			applyPremiumTitleSign(terrainPreference);
		}

		terrainPreference.setSummary(prepareRatingSummary(terrainMin, terrainMax));
	}

	private void prepareDistancePreference() {
		final EditTextPreference distancePreference = findPreference(FILTER_DISTANCE, EditTextPreference.class);
		final EditText filterDistanceEditText = distancePreference.getEditText();
		filterDistanceEditText.setKeyListener(DigitsKeyListener.getInstance(false, true));

		// set summary text
		if (!mImperialUnits) {
			distancePreference.setSummary(preparePreferenceSummary(distancePreference.getText() + UNIT_KM, R.string.pref_distance_summary_km));
		} else {
			distancePreference.setDialogMessage(R.string.pref_distance_summary_miles);
			distancePreference.setSummary(preparePreferenceSummary(distancePreference.getText() + UNIT_MILES, R.string.pref_distance_summary_miles));
		}
	}

	private CharSequence prepareRatingSummary(CharSequence min, CharSequence max) {
		return preparePreferenceSummary(min + " - " + max, 0);
	}

	private CharSequence prepareCacheTypeSummary() {
		StringBuilder sb = new StringBuilder();

		boolean allChecked = true;
		boolean noneChecked = true;

		final int len = GeocacheType.values().length;
		for (int i = 0; i < len; i++) {
			if (mPrefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
				noneChecked = false;
			} else {
				allChecked = false;
			}
		}

		if (allChecked || noneChecked) {
			sb.append(getString(R.string.pref_geocache_type_all));
		} else {
			for (int i = 0; i < len; i++) {
				if (mPrefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
					if (sb.length() != 0) sb.append(TEXT_VALUE_SEPARATOR);
					sb.append(shortCacheTypeName[i]);
				}
			}
		}

		return preparePreferenceSummary(sb.toString(), 0);
	}

	private CharSequence prepareCacheTypeSummaryBasicMember() {
		return preparePreferenceSummary(shortCacheTypeName[GeocacheType.Traditional.ordinal()] +
				TEXT_VALUE_SEPARATOR +
				shortCacheTypeName[GeocacheType.Event.ordinal()] +
				TEXT_VALUE_SEPARATOR +
				shortCacheTypeName[GeocacheType.CacheInTrashOutEvent.ordinal()],
				0);
	}

	private CharSequence prepareContainerTypeSummary() {
		StringBuilder sb = new StringBuilder();

		final int len = ContainerType.values().length;
		for (int i = 0; i < len; i++) {
			if (mPrefs.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
				if (sb.length() != 0) sb.append(TEXT_VALUE_SEPARATOR);
				sb.append(shortContainerTypeName[i]);
			}
		}

		if (sb.length() == 0) {
			for (int i = 0; i < len; i++) {
				if (sb.length() != 0) sb.append(TEXT_VALUE_SEPARATOR);
				sb.append(shortContainerTypeName[i]);
			}
		}

		return preparePreferenceSummary(sb.toString(), 0);
	}

	private CharSequence prepareContainerTypeSummaryBasicMember() {
		StringBuilder sb = new StringBuilder();

		final int len = ContainerType.values().length;
		for (int i = 0; i < len; i++) {
			if (sb.length() != 0) sb.append(", ");
			sb.append(shortContainerTypeName[i]);
		}

		return preparePreferenceSummary(sb.toString(), 0);
	}

}
