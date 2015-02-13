package com.arcao.geocaching4locus.fragment.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import com.arcao.geocaching.api.data.type.CacheType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.preference.ListPreference;

public class FilterPreferenceFragment extends AbstractPreferenceFragment {
	public static final String PARAM_SCREEN__CACHE_TYPE = "CACHE_TYPE";
	public static final String PARAM_SCREEN__CONTAINER_TYPE = "CONTAINER_TYPE";
	public static final String PARAM_SCREEN__DIFFICULTY = "DIFFICULTY";
	public static final String PARAM_SCREEN__TERRAIN = "TERRAIN";

	private boolean mPremiumMember;
	private boolean mImperialUnits;
	private String mSubScreenKey;

	@Override
	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preference_category_filter);

		mPremiumMember = App.get(getActivity()).getAuthenticatorHelper().getRestrictions().isPremiumMember();
		mImperialUnits = mPrefs.getBoolean(PrefConstants.IMPERIAL_UNITS, false);
	}

	@Override
	protected void preparePreference() {
		mSubScreenKey = getSubScreenKey();
		if (mSubScreenKey != null) {
			switch (mSubScreenKey) {
				case PARAM_SCREEN__CACHE_TYPE:
					setHasOptionsMenu(true);
					setPreferenceScreen(findPreference(FILTER_CACHE_TYPE, PreferenceScreen.class));
					break;

				case PARAM_SCREEN__CONTAINER_TYPE:
					setHasOptionsMenu(true);
					setPreferenceScreen(findPreference(FILTER_CONTAINER_TYPE, PreferenceScreen.class));
					break;

				case PARAM_SCREEN__DIFFICULTY:
					setPreferenceScreen(findPreference(FILTER_DIFFICULTY, PreferenceScreen.class));
					break;

				case PARAM_SCREEN__TERRAIN:
					setPreferenceScreen(findPreference(FILTER_TERRAIN, PreferenceScreen.class));
					break;
			}

			CharSequence title =  getPreferenceScreen().getTitle();
			if (title != null) {
				getActivity().setTitle(title);
			}
		}

		final PreferenceScreen cacheTypeFilterScreen = findPreference(FILTER_CACHE_TYPE, PreferenceScreen.class);
		if (cacheTypeFilterScreen != null) {
			prepareCacheTypePreference(cacheTypeFilterScreen);
		}

		final PreferenceScreen containerTypeFilterScreen = findPreference(FILTER_CONTAINER_TYPE, PreferenceScreen.class);
		if (containerTypeFilterScreen != null) {
			prepareContainerTypePreference(containerTypeFilterScreen);
		}

		final Preference difficultyPreference = findPreference(FILTER_DIFFICULTY, Preference.class);
		if (difficultyPreference != null) {
			prepareDifficultyPreference(difficultyPreference);
		}

		final Preference terrainPreference = findPreference(FILTER_TERRAIN, Preference.class);
		if (terrainPreference != null) {
			prepareTerrainPreference(terrainPreference);
		}

		final EditTextPreference distancePreference = findPreference(FILTER_DISTANCE, EditTextPreference.class);
		if (distancePreference != null) {
			prepareDistancePreference(distancePreference);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (PARAM_SCREEN__CACHE_TYPE.equals(mSubScreenKey) || PARAM_SCREEN__CONTAINER_TYPE.equals(mSubScreenKey)) {
			inflater.inflate(R.menu.select_deselect_acttionbar, menu);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
				getActivity().finish();
				return true;

			case R.id.selectAll:
				if (PARAM_SCREEN__CACHE_TYPE.equals(mSubScreenKey)) {
					for (int i = 0; i < CacheType.values().length; i++)
						findPreference(FILTER_CACHE_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(true);
				}
				else if (PARAM_SCREEN__CONTAINER_TYPE.equals(mSubScreenKey)) {
					for (int i = 0; i < ContainerType.values().length; i++)
						findPreference(FILTER_CONTAINER_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(true);
				}
				return true;

			case R.id.deselectAll:
				if (PARAM_SCREEN__CACHE_TYPE.equals(mSubScreenKey)) {
					for (int i = 0; i < CacheType.values().length; i++)
						findPreference(FILTER_CACHE_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(false);
				}
				else if (PARAM_SCREEN__CONTAINER_TYPE.equals(mSubScreenKey)) {
					for (int i = 0; i < ContainerType.values().length; i++)
						findPreference(FILTER_CONTAINER_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(false);
				}
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
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

			case FILTER_DIFFICULTY_MIN:
			case FILTER_DIFFICULTY_MAX:
			case FILTER_TERRAIN_MIN:
			case FILTER_TERRAIN_MAX:
				final ListPreference difficultyTerrainPreference = findPreference(key, ListPreference.class);
				difficultyTerrainPreference.setSummary(prepareRatingSummary(difficultyTerrainPreference.getEntry()));
				break;
		}
	}

	private void prepareCacheTypePreference(PreferenceScreen cacheTypeFilterScreen) {
		cacheTypeFilterScreen.setIntent(createSubScreenIntent(PARAM_SCREEN__CACHE_TYPE));
		cacheTypeFilterScreen.setEnabled(mPremiumMember);

		if (mPremiumMember)
			cacheTypeFilterScreen.setSummary(prepareCacheTypeSummary());
	}

	private void prepareContainerTypePreference(PreferenceScreen containerTypeFilterScreen) {
		containerTypeFilterScreen.setIntent(createSubScreenIntent(PARAM_SCREEN__CONTAINER_TYPE));
		containerTypeFilterScreen.setEnabled(mPremiumMember);

		if (mPremiumMember)
			containerTypeFilterScreen.setSummary(prepareContainerTypeSummary());
	}

	private void prepareDifficultyPreference(Preference difficultyPreference) {
		final ListPreference difficultyMinPreference = findPreference(FILTER_DIFFICULTY_MIN, ListPreference.class);
		final ListPreference difficultyMaxPreference = findPreference(FILTER_DIFFICULTY_MAX, ListPreference.class);

		difficultyPreference.setIntent(createSubScreenIntent(PARAM_SCREEN__DIFFICULTY));
		difficultyPreference.setEnabled(mPremiumMember);

		difficultyMinPreference.setSummary(prepareRatingSummary(difficultyMinPreference.getValue()));
		difficultyMinPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat((String) newValue);
				float max = Float.parseFloat(difficultyMaxPreference.getValue());

				if (min > max) {
					difficultyMaxPreference.setValue((String) newValue);
					difficultyMaxPreference.setSummary(prepareRatingSummary((CharSequence) newValue));
				}
				return true;
			}
		});

		difficultyMaxPreference.setSummary(prepareRatingSummary(difficultyMaxPreference.getValue()));
		difficultyMaxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat(difficultyMinPreference.getValue());
				float max = Float.parseFloat((String) newValue);

				if (min > max) {
					difficultyMinPreference.setValue((String) newValue);
					difficultyMinPreference.setSummary(prepareRatingSummary((CharSequence) newValue));
				}
				return true;
			}
		});

		if (mPremiumMember)
			difficultyPreference.setSummary(prepareRatingSummary(difficultyMinPreference.getValue(), difficultyMaxPreference.getValue()));
	}

	private void prepareTerrainPreference(Preference terrainPreference) {
		final ListPreference terrainMinPreference = findPreference(FILTER_TERRAIN_MIN, ListPreference.class);
		final ListPreference terrainMaxPreference = findPreference(FILTER_TERRAIN_MAX, ListPreference.class);

		terrainPreference.setIntent(createSubScreenIntent(PARAM_SCREEN__TERRAIN));
		terrainPreference.setEnabled(mPremiumMember);

		terrainMinPreference.setSummary(prepareRatingSummary(terrainMinPreference.getValue()));
		terrainMinPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat((String) newValue);
				float max = Float.parseFloat(terrainMaxPreference.getValue());

				if (min > max) {
					terrainMaxPreference.setValue((String) newValue);
					terrainMaxPreference.setSummary(prepareRatingSummary((String) newValue));
				}
				return true;
			}
		});

		terrainMaxPreference.setSummary(prepareRatingSummary(terrainMaxPreference.getValue()));
		terrainMaxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat(terrainMinPreference.getValue());
				float max = Float.parseFloat((String) newValue);

				if (min > max) {
					terrainMinPreference.setValue((String) newValue);
					terrainMinPreference.setSummary(prepareRatingSummary((String) newValue));
				}
				return true;
			}
		});

		if (mPremiumMember)
			terrainPreference.setSummary(prepareRatingSummary(terrainMinPreference.getValue(), terrainMaxPreference.getValue()));
	}

	private void prepareDistancePreference(EditTextPreference distancePreference) {
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
		return preparePreferenceSummary(min.toString() + " - " + max.toString(), 0);
	}

	private CharSequence prepareRatingSummary(CharSequence value) {
		return preparePreferenceSummary(value, 0);
	}

	private CharSequence prepareCacheTypeSummary() {
		StringBuilder sb = new StringBuilder();

		boolean allChecked = true;
		boolean noneChecked = true;

		for (int i = 0; i < CacheType.values().length; i++) {
			if (mPrefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
				noneChecked = false;
			} else {
				allChecked = false;
			}
		}

		if (allChecked || noneChecked) {
			sb.append(getString(R.string.pref_cache_type_all));
		} else {
			for (int i = 0; i < CacheType.values().length; i++) {
				if (mPrefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
					if (sb.length() != 0) sb.append(", ");
					sb.append(shortCacheTypeName[i]);
				}
			}
		}

		return preparePreferenceSummary(sb.toString(), 0);
	}

	private CharSequence prepareContainerTypeSummary() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ContainerType.values().length; i++) {
			if (mPrefs.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
				if (sb.length() != 0) sb.append(", ");
				sb.append(shortContainerTypeName[i]);
			}
		}

		if (sb.length() == 0) {
			for (int i = 0; i < ContainerType.values().length; i++) {
				if (sb.length() != 0) sb.append(", ");
				sb.append(shortContainerTypeName[i]);
			}
		}

		return preparePreferenceSummary(sb.toString(), 0);
	}
}
