package com.arcao.geocaching4locus;

import geocaching.api.data.type.CacheType;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.hlidskialf.android.preference.SeekBarPreference;

public class PreferenceActivity extends android.preference.PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
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
		
		preparePreferences();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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
		
		SeekBarPreference filterCountOfCachesPreference = findPreference("filter_count_of_caches", SeekBarPreference.class);
		filterCountOfCachesPreference.setSummary(preparePreferenceSummary(String.valueOf(filterCountOfCachesPreference.getProgress()), R.string.pref_count_of_caches_summary));		
		
		SeekBarPreference downloadingCountOfLogsPreference = findPreference("downloading_count_of_logs", SeekBarPreference.class);
		downloadingCountOfLogsPreference.setSummary(preparePreferenceSummary(String.valueOf(downloadingCountOfLogsPreference.getProgress()), R.string.pref_count_of_logs_summary));
		
		SeekBarPreference downloadingCountOfTrackablesPreference = findPreference("downloading_count_of_trackables", SeekBarPreference.class);
		downloadingCountOfTrackablesPreference.setSummary(preparePreferenceSummary(String.valueOf(downloadingCountOfTrackablesPreference.getProgress()), R.string.pref_count_of_trackables_summary));
		
		EditTextPreference usernamePreference = findPreference("username", EditTextPreference.class);
		usernamePreference.setSummary(prepareRequiredPreferenceSummary(usernamePreference.getText(), 0, true));
		
		EditTextPreference passwordPreference = findPreference("password", EditTextPreference.class);
		passwordPreference.setSummary(prepareRequiredPreferenceSummary(passwordPreference.getText(), 0, false));
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
	
	protected Spanned preparePreferenceSummary(String value, int resId) {
		String summary = "";
		if (resId != 0)
			summary = getText(resId).toString();
		
    if (value != null && value.length() > 0) 
    	return Html.fromHtml("<font color=\"#FF8000\"><b>(" + value + ")</b></font> " + summary);
    return Html.fromHtml(summary);
  }
	
	protected Spanned prepareRequiredPreferenceSummary(String value, int resId, boolean addValue) {
		String summary = "";
		if (resId != 0)
			summary = getText(resId).toString();

		if (value == null || value.length() == 0)
			return Html.fromHtml("<font color=\"#FF0000\"><b>(" + getText(R.string.pref_not_filled) + ")</b></font> " + summary);
		if (addValue)
			return preparePreferenceSummary(value, resId);
		return Html.fromHtml(summary);
  }
	
	@SuppressWarnings("unchecked")
	protected <T extends Preference> T findPreference(String key, Class<T> clazz) {
		return (T) getPreferenceScreen().findPreference(key);
	}
}
