package com.arcao.geocaching4locus;

import geocaching.api.data.type.CacheType;
import android.app.AlertDialog;
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

import com.arcao.preference.ListPreference;
import com.hlidskialf.android.preference.SeekBarPreference;

public class PreferenceActivity extends android.preference.PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String TAG = "Geocaching4Locus|PreferenceActivity";
	
	protected static final Uri WEBSITE_URI = Uri.parse("http://g4l.arcao.com");
	protected static final String DONATE_PAYPAL_URI_STRING = "https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=arcao%%40arcao%%2ecom&lc=CZ&item_name=Geocaching4Locus&item_number=g4l&currency_code=%s&bn=PP%%2dDonationsBF%%3abtn_donateCC_LG%%2egif%%3aNonHosted";
	
	protected static final String UNIT_KM = "km";
	protected static final String UNIT_MILES = "mi";
	
	private SharedPreferences prefs;
	private PreferenceScreen cacheTypeFilterScreen;
	
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
		
		if (getIntent().getBooleanExtra("ShowCacheTypeFilterScreen", false)) {
			setPreferenceScreen(cacheTypeFilterScreen);
			return;
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
		if (!getIntent().getBooleanExtra("ShowCacheTypeFilterScreen", false)) {
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
		boolean imperialUnits = prefs.getBoolean("imperial_units", false);
		
		if ("username".equals(key)) {
			final EditTextPreference p = findPreference(key, EditTextPreference.class);  
			p.setSummary(prepareRequiredPreferenceSummary(p.getText(), 0, true));
		} else if ("password".equals(key)) {
			final EditTextPreference p = findPreference(key, EditTextPreference.class);  
			p.setSummary(prepareRequiredPreferenceSummary(p.getText(), 0, false));
		} else if ("filter_distance".equals(key) && !imperialUnits) {
			final EditTextPreference p = findPreference(key, EditTextPreference.class);
			p.setSummary(preparePreferenceSummary(p.getText() + UNIT_KM, R.string.pref_distance_summary_km));
		} else if ("filter_distance".equals(key) && imperialUnits) {
			final EditTextPreference p = findPreference(key, EditTextPreference.class);  
			p.setSummary(preparePreferenceSummary(p.getText() + UNIT_MILES, R.string.pref_distance_summary_miles));
		} else if ("filter_count_of_caches".equals(key)) {
			final SeekBarPreference p = findPreference(key, SeekBarPreference.class);
			p.setSummary(preparePreferenceSummary(String.valueOf(p.getProgress()), R.string.pref_count_of_caches_summary));
		} else if ("downloading_count_of_logs".equals(key)) {
			final SeekBarPreference p = findPreference(key, SeekBarPreference.class);
			p.setSummary(preparePreferenceSummary(String.valueOf(p.getProgress()), R.string.pref_count_of_logs_summary));
		} else if ("downloading_count_of_trackables".equals(key)) {
			final SeekBarPreference p = findPreference(key, SeekBarPreference.class);
			p.setSummary(preparePreferenceSummary(String.valueOf(p.getProgress()), R.string.pref_count_of_trackables_summary));
		} else if ("difficulty_filter_min".equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), 0));
		} else if ("difficulty_filter_max".equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), 0));			
		} else if ("terrain_filter_min".equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), 0));
		} else if ("terrain_filter_max".equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), 0));
		} else if ("full_cache_data_on_show".equals(key)) {
			final ListPreference p = findPreference(key, ListPreference.class);
			p.setSummary(preparePreferenceSummary(p.getEntry(), R.string.pref_download_on_show_summary));
		}
	}
	
	protected void preparePreferences() {
		final EditTextPreference filterDistancePreference = findPreference("filter_distance", EditTextPreference.class);
		final EditText filterDistanceEditText = filterDistancePreference.getEditText(); 
		filterDistanceEditText.setKeyListener(DigitsKeyListener.getInstance(false,true));
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// remove old session login
		Editor edit = prefs.edit();
		edit.remove("session");
		edit.commit();
		
		boolean imperialUnits = prefs.getBoolean("imperial_units", false);
				
		final CheckBoxPreference imperialUnitsPreference = findPreference("imperial_units", CheckBoxPreference.class);
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
		
		final SeekBarPreference filterCountOfCachesPreference = findPreference("filter_count_of_caches", SeekBarPreference.class);
		filterCountOfCachesPreference.setSummary(preparePreferenceSummary(String.valueOf(filterCountOfCachesPreference.getProgress()), R.string.pref_count_of_caches_summary));		
		
		final SeekBarPreference downloadingCountOfLogsPreference = findPreference("downloading_count_of_logs", SeekBarPreference.class);
		downloadingCountOfLogsPreference.setSummary(preparePreferenceSummary(String.valueOf(downloadingCountOfLogsPreference.getProgress()), R.string.pref_count_of_logs_summary));
		
		final SeekBarPreference downloadingCountOfTrackablesPreference = findPreference("downloading_count_of_trackables", SeekBarPreference.class);
		downloadingCountOfTrackablesPreference.setSummary(preparePreferenceSummary(String.valueOf(downloadingCountOfTrackablesPreference.getProgress()), R.string.pref_count_of_trackables_summary));
		
		final EditTextPreference usernamePreference = findPreference("username", EditTextPreference.class);
		usernamePreference.setSummary(prepareRequiredPreferenceSummary(usernamePreference.getText(), 0, true));
		
		final EditTextPreference passwordPreference = findPreference("password", EditTextPreference.class);
		passwordPreference.setSummary(prepareRequiredPreferenceSummary(passwordPreference.getText(), 0, false));
		
		final Preference websitePreference = findPreference("website", Preference.class);
		websitePreference.setIntent(new Intent(Intent.ACTION_VIEW, WEBSITE_URI));
		
		final Preference donatePaypalPreference = findPreference("donate_paypal", Preference.class);
		donatePaypalPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				donatePaypal();
				return true;
			}
		});
		
		final ListPreference difficultyMinPreference = findPreference("difficulty_filter_min", ListPreference.class);
		final ListPreference difficultyMaxPreference = findPreference("difficulty_filter_max", ListPreference.class);
		
		difficultyMinPreference.setSummary(prepareRatingSummary(difficultyMinPreference.getValue()));
		difficultyMinPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat((String)newValue); 
				float max = Float.parseFloat(difficultyMaxPreference.getValue());
				
				if (min > max) {
					difficultyMaxPreference.setValue((String)newValue);
					difficultyMaxPreference.setSummary(prepareRatingSummary((String)newValue));
				}
				return true;
			}
		});
		
		difficultyMaxPreference.setSummary(prepareRatingSummary(difficultyMaxPreference.getValue()));
		difficultyMaxPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat(difficultyMinPreference.getValue()); 
				float max = Float.parseFloat((String)newValue);
				
				if (min > max) {
					difficultyMinPreference.setValue((String)newValue);
					difficultyMinPreference.setSummary(prepareRatingSummary((String)newValue));
				}				
				return true;
			}
		});

		final ListPreference terrainMinPreference = findPreference("terrain_filter_min", ListPreference.class);
		final ListPreference terrainMaxPreference = findPreference("terrain_filter_max", ListPreference.class);
		
		terrainMinPreference.setSummary(prepareRatingSummary(terrainMinPreference.getValue()));
		terrainMinPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat((String)newValue); 
				float max = Float.parseFloat(terrainMaxPreference.getValue());
				
				if (min > max) {
					terrainMaxPreference.setValue((String)newValue);
					terrainMaxPreference.setSummary(prepareRatingSummary((String)newValue));
				}
				return true;
			}
		});
		
		terrainMaxPreference.setSummary(prepareRatingSummary(terrainMaxPreference.getValue()));
		terrainMaxPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float min = Float.parseFloat(terrainMinPreference.getValue()); 
				float max = Float.parseFloat((String)newValue);
				
				if (min > max) {
					terrainMinPreference.setValue((String)newValue);
					terrainMinPreference.setSummary(prepareRatingSummary((String)newValue));
				}
				return true;
			}
		});
		
		final CheckBoxPreference simpleCacheDataPreference = findPreference("simple_cache_data", CheckBoxPreference.class);
		final ListPreference fullCacheDataOnShowPreference = findPreference("full_cache_data_on_show", ListPreference.class);
		simpleCacheDataPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				fullCacheDataOnShowPreference.setEnabled((Boolean) newValue);
				return true;
			}
		});
		fullCacheDataOnShowPreference.setEnabled(simpleCacheDataPreference.isChecked());
		fullCacheDataOnShowPreference.setSummary(preparePreferenceSummary(fullCacheDataOnShowPreference.getEntry(), R.string.pref_download_on_show_summary));
		
		final Preference versionPreference = findPreference("version", Preference.class);
		versionPreference.setSummary(getVersion(this));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getPreferenceScreen().equals(cacheTypeFilterScreen)) {
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
				for (int i = 0; i < CacheType.values().length; i++)
					findPreference("filter_" + i, CheckBoxPreference.class).setChecked(true);
				return true;
			case R.id.deselectAll:
				for (int i = 0; i < CacheType.values().length; i++)
					findPreference("filter_" + i, CheckBoxPreference.class).setChecked(false);
				return true;
			default:
				return super.onOptionsItemSelected(item);
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
	
	protected Spanned prepareRatingSummary(CharSequence min, CharSequence max) {	
   	return preparePreferenceSummary(min.toString() + " - " + max.toString(), 0);
  }
	
	protected Spanned prepareRatingSummary(CharSequence value) {	
   	return preparePreferenceSummary(value, 0);
  }
	
	protected void donatePaypal() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pref_donate_paypal_choose_currency);
		builder.setSingleChoiceItems(R.array.currency, -1, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				startActivity(new Intent(
						Intent.ACTION_VIEW, 
						Uri.parse(String.format(DONATE_PAYPAL_URI_STRING, getResources().getStringArray(R.array.currency)[which]))
				));
			}
		});
		builder.setCancelable(true);
		builder.show();
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
