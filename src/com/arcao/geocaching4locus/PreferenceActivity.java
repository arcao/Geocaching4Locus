package com.arcao.geocaching4locus;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.arcao.geocaching.api.data.type.CacheType;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.preference.ListPreference;
import com.hlidskialf.android.preference.SeekBarPreference;

public class PreferenceActivity extends android.preference.PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, PrefConstants {
	private static final String TAG = "G4L|PreferenceActivity";

	public static final int DIALOG_DONATE_ID = 0;
	
	protected static final String ACCOUNT = "account";
	
	protected static final String UNIT_KM = "km";
	protected static final String UNIT_MILES = "mi";

	private SharedPreferences prefs;
	private PreferenceScreen cacheTypeFilterScreen;
	private PreferenceScreen containerTypeFilterScreen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		cacheTypeFilterScreen = (PreferenceScreen) findPreference("cache_type_filter_screen");
		if (cacheTypeFilterScreen != null) {
			Intent intent = new Intent(this, PreferenceActivity.class);
			intent.putExtra("ShowCacheTypeFilterScreen", true);
			cacheTypeFilterScreen.setIntent(intent);
		}
		
		containerTypeFilterScreen = (PreferenceScreen) findPreference("container_type_filter_screen");
		if (containerTypeFilterScreen != null) {
			Intent intent = new Intent(this, PreferenceActivity.class);
			intent.putExtra("ShowContainerTypeFilterScreen", true);
			containerTypeFilterScreen.setIntent(intent);
		}

		if (getIntent().getBooleanExtra("ShowCacheTypeFilterScreen", false)) {
			setPreferenceScreen(cacheTypeFilterScreen);
			return;
		}
		
		if (getIntent().getBooleanExtra("ShowContainerTypeFilterScreen", false)) {
			setPreferenceScreen(containerTypeFilterScreen);
			return;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		if (!getIntent().getBooleanExtra("ShowCacheTypeFilterScreen", false)
				&& !getIntent().getBooleanExtra("ShowContainerTypeFilterScreen", false)) {
			preparePreferences();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		boolean imperialUnits = prefs.getBoolean(PrefConstants.IMPERIAL_UNITS, false);

		if (FILTER_DISTANCE.equals(key) && !imperialUnits) {
			final EditTextPreference p = findPreference(key, EditTextPreference.class);
			p.setSummary(preparePreferenceSummary(p.getText() + UNIT_KM, R.string.pref_distance_summary_km));
		} else if (FILTER_DISTANCE.equals(key) && imperialUnits) {
			final EditTextPreference p = findPreference(key, EditTextPreference.class);
			p.setSummary(preparePreferenceSummary(p.getText() + UNIT_MILES, R.string.pref_distance_summary_miles));
		} else if (DOWNLOADING_COUNT_OF_CACHES.equals(key)) {
			final SeekBarPreference p = findPreference(key, SeekBarPreference.class);
			p.setSummary(preparePreferenceSummary(String.valueOf(p.getProgress()), R.string.pref_count_of_caches_summary));
		} else if (DOWNLOADING_COUNT_OF_LOGS.equals(key)) {
			final SeekBarPreference p = findPreference(key, SeekBarPreference.class);
			p.setSummary(preparePreferenceSummary(String.valueOf(p.getProgress()), R.string.pref_count_of_logs_summary));
//		} else if (DOWNLOADING_COUNT_OF_TRACKABLES.equals(key)) {
//			final SeekBarPreference p = findPreference(key, SeekBarPreference.class);
//			p.setSummary(preparePreferenceSummary(String.valueOf(p.getProgress()), R.string.pref_count_of_trackables_summary));
		} else if (FILTER_DIFFICULTY_MIN.equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), 0));
		} else if (FILTER_DIFFICULTY_MAX.equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), 0));
		} else if (FILTER_TERRAIN_MIN.equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), 0));
		} else if (FILTER_TERRAIN_MAX.equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), 0));
		} else if (DOWNLOADING_FULL_CACHE_DATE_ON_SHOW.equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), R.string.pref_download_on_show_summary));
		}
	}

	protected void preparePreferences() {
		final EditTextPreference filterDistancePreference = findPreference(FILTER_DISTANCE, EditTextPreference.class);
		final EditText filterDistanceEditText = filterDistancePreference.getEditText();
		filterDistanceEditText.setKeyListener(DigitsKeyListener.getInstance(false, true));

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// remove old session login
		Editor edit = prefs.edit();
		edit.remove("session");
		edit.commit();

		boolean imperialUnits = prefs.getBoolean(IMPERIAL_UNITS, false);

		final CheckBoxPreference imperialUnitsPreference = findPreference(IMPERIAL_UNITS, CheckBoxPreference.class);
		imperialUnitsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float distance = Float.parseFloat(filterDistancePreference.getText());
				if (((Boolean) newValue)) {
					filterDistancePreference.setText(Float.toString(distance / 1.609344F));
					filterDistancePreference.setSummary(preparePreferenceSummary(Float.toString(distance / 1.609344F) + UNIT_MILES, R.string.pref_distance_summary_miles));
					filterDistancePreference.setDialogMessage(R.string.pref_distance_summary_miles);
				} else {
					filterDistancePreference.setText(Float.toString(distance * 1.609344F));
					filterDistancePreference.setSummary(preparePreferenceSummary(Float.toString(distance * 1.609344F) + UNIT_KM, R.string.pref_distance_summary_km));
					filterDistancePreference.setDialogMessage(R.string.pref_distance_summary_km);
				}
				return true;
			}
		});

		// set summary text
		if (!imperialUnits) {
			filterDistancePreference.setSummary(preparePreferenceSummary(filterDistancePreference.getText() + UNIT_KM, R.string.pref_distance_summary_km));
		} else {
			filterDistancePreference.setDialogMessage(R.string.pref_distance_summary_miles);
			filterDistancePreference.setSummary(preparePreferenceSummary(filterDistancePreference.getText() + UNIT_MILES, R.string.pref_distance_summary_miles));
		}

		final SeekBarPreference filterCountOfCachesPreference = findPreference(DOWNLOADING_COUNT_OF_CACHES, SeekBarPreference.class);
		filterCountOfCachesPreference.setSummary(preparePreferenceSummary(String.valueOf(filterCountOfCachesPreference.getProgress()),
				R.string.pref_count_of_caches_summary));

		final SeekBarPreference downloadingCountOfLogsPreference = findPreference(DOWNLOADING_COUNT_OF_LOGS, SeekBarPreference.class);
		downloadingCountOfLogsPreference.setSummary(preparePreferenceSummary(String.valueOf(downloadingCountOfLogsPreference.getProgress()),
				R.string.pref_count_of_logs_summary));

//		final SeekBarPreference downloadingCountOfTrackablesPreference = findPreference(DOWNLOADING_COUNT_OF_TRACKABLES, SeekBarPreference.class);
//		downloadingCountOfTrackablesPreference.setSummary(preparePreferenceSummary(String.valueOf(downloadingCountOfTrackablesPreference.getProgress()),
//				R.string.pref_count_of_trackables_summary));

		prepareAccountPreference();

		final Preference websitePreference = findPreference(ABOUT_WEBSITE, Preference.class);
		websitePreference.setIntent(new Intent(Intent.ACTION_VIEW, AppConstants.WEBSITE_URI));

		final Preference donatePaypalPreference = findPreference(ABOUT_DONATE_PAYPAL, Preference.class);
		donatePaypalPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				donatePaypal();
				return true;
			}
		});

		final ListPreference difficultyMinPreference = findPreference(FILTER_DIFFICULTY_MIN, ListPreference.class);
		final ListPreference difficultyMaxPreference = findPreference(FILTER_DIFFICULTY_MAX, ListPreference.class);

		difficultyMinPreference.setSummary(prepareRatingSummary(difficultyMinPreference.getValue()));
		difficultyMinPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat((String) newValue);
				float max = Float.parseFloat(difficultyMaxPreference.getValue());

				if (min > max) {
					difficultyMaxPreference.setValue((String) newValue);
					difficultyMaxPreference.setSummary(prepareRatingSummary((String) newValue));
				}
				return true;
			}
		});

		difficultyMaxPreference.setSummary(prepareRatingSummary(difficultyMaxPreference.getValue()));
		difficultyMaxPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat(difficultyMinPreference.getValue());
				float max = Float.parseFloat((String) newValue);

				if (min > max) {
					difficultyMinPreference.setValue((String) newValue);
					difficultyMinPreference.setSummary(prepareRatingSummary((String) newValue));
				}
				return true;
			}
		});

		final ListPreference terrainMinPreference = findPreference(FILTER_TERRAIN_MIN, ListPreference.class);
		final ListPreference terrainMaxPreference = findPreference(FILTER_TERRAIN_MAX, ListPreference.class);

		terrainMinPreference.setSummary(prepareRatingSummary(terrainMinPreference.getValue()));
		terrainMinPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
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
		terrainMaxPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
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

		final CheckBoxPreference simpleCacheDataPreference = findPreference(DOWNLOADING_SIMPLE_CACHE_DATA, CheckBoxPreference.class);
		final ListPreference fullCacheDataOnShowPreference = findPreference(DOWNLOADING_FULL_CACHE_DATE_ON_SHOW, ListPreference.class);
		simpleCacheDataPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				fullCacheDataOnShowPreference.setEnabled((Boolean) newValue);
				return true;
			}
		});
		fullCacheDataOnShowPreference.setEnabled(simpleCacheDataPreference.isChecked());
		fullCacheDataOnShowPreference.setSummary(preparePreferenceSummary(fullCacheDataOnShowPreference.getEntry(), R.string.pref_download_on_show_summary));

		final Preference versionPreference = findPreference(ABOUT_VERSION, Preference.class);
		versionPreference.setSummary(getVersion(this));
	}
	
	protected void prepareAccountPreference() {
		final Preference accountPreference = findPreference(ACCOUNT, EditTextPreference.class);
		accountPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if (Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount()) {
					Geocaching4LocusApplication.getAuthenticatorHelper().removeAccount();
					accountPreference.setTitle(R.string.pref_account_login);
					accountPreference.setSummary(R.string.pref_account_login_summary);
				} else {
					try {
						Geocaching4LocusApplication.getAuthenticatorHelper().addAccount(PreferenceActivity.this);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
				
				return true;
			}
		});
		
		if (Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount()) {
			accountPreference.setTitle(R.string.pref_account_logout);
			accountPreference.setSummary(prepareAcountSummary(Geocaching4LocusApplication.getAuthenticatorHelper().getAccount().name, R.string.pref_account_logout_summary));
		} else {
			accountPreference.setTitle(R.string.pref_account_login);
			accountPreference.setSummary(R.string.pref_account_login_summary);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getPreferenceScreen().equals(cacheTypeFilterScreen)
				|| getPreferenceScreen().equals(containerTypeFilterScreen)) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.cache_type_option_menu, menu);
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				// app icon in action bar clicked; go home
				finish();
				return true;
			case R.id.selectAll:
				if (getPreferenceScreen().equals(cacheTypeFilterScreen)) {
					for (int i = 0; i < CacheType.values().length; i++)
						findPreference(FILTER_CACHE_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(true);
				} else if (getPreferenceScreen().equals(containerTypeFilterScreen)) {
					for (int i = 0; i < ContainerType.values().length; i++)
						findPreference(FILTER_CONTAINER_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(true);
				}
				return true;
			case R.id.deselectAll:
				if (getPreferenceScreen().equals(cacheTypeFilterScreen)) {
					for (int i = 0; i < CacheType.values().length; i++)
						findPreference(FILTER_CACHE_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(false);
				} else if (getPreferenceScreen().equals(containerTypeFilterScreen)) {
					for (int i = 0; i < ContainerType.values().length; i++)
						findPreference(FILTER_CONTAINER_TYPE_PREFIX + i, CheckBoxPreference.class).setChecked(false);
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_DONATE_ID:
				return new AlertDialog.Builder(this)
					.setTitle(R.string.pref_donate_paypal_choose_currency)
					.setSingleChoiceItems(R.array.currency, -1, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							startActivity(new Intent(
									Intent.ACTION_VIEW,
									Uri.parse(String.format(AppConstants.DONATE_PAYPAL_URI, getResources().getStringArray(R.array.currency)[which]))
							));
						}
					})
					.setCancelable(true)
					.create();

			default:
				return super.onCreateDialog(id);
		}
	}

	protected Spanned preparePreferenceSummary(CharSequence value, int resId) {
		String summary = "";
		if (resId != 0)
			summary = getText(resId).toString();

		if (value != null && value.length() > 0)
			return Html.fromHtml("<font color=\"#FF8000\"><b>(" + value.toString() + ")</b></font> " + summary);
		return Html.fromHtml(summary);
	}

	protected Spanned prepareRequiredPreferenceSummary(CharSequence value, int resId, boolean addValue) {
		String summary = "";
		if (resId != 0)
			summary = getText(resId).toString();

		if (value == null || value.length() == 0)
			return Html.fromHtml("<font color=\"#FF0000\"><b>(" + getText(R.string.pref_not_filled) + ")</b></font> " + summary);
		if (addValue)
			return preparePreferenceSummary(value, resId);
		return Html.fromHtml(summary);
	}
	
	protected Spanned prepareAcountSummary(CharSequence value, int resId) {
		String summary = "%s";
		if (resId != 0)
			summary = getText(resId).toString();

		return Html.fromHtml(String.format(summary, "<font color=\"#FF8000\"><b>" + value.toString() + "</b></font>"));
	}

	protected Spanned prepareRatingSummary(CharSequence min, CharSequence max) {
		return preparePreferenceSummary(min.toString() + " - " + max.toString(), 0);
	}

	protected Spanned prepareRatingSummary(CharSequence value) {
		return preparePreferenceSummary(value, 0);
	}

	protected void donatePaypal() {
		showDialog(DIALOG_DONATE_ID);
	}

	protected String getVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			return "?";
		}
	}

	@SuppressWarnings("unchecked")
	protected <T extends Preference> T findPreference(String key, Class<T> clazz) {
		return (T) getPreferenceScreen().findPreference(key);
	}
}
