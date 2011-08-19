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
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class PreferenceActivity extends android.preference.PreferenceActivity {
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

		
		final EditTextPreference filterDistancePreference = (EditTextPreference) findPreference("filter_distance");
		final EditText filterDistanceEditText = filterDistancePreference.getEditText(); 
		filterDistanceEditText.setKeyListener(DigitsKeyListener.getInstance(false,true));
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		// remove old session login
		Editor edit = prefs.edit();
		edit.remove("session");
		edit.commit();
		
		if (prefs.getBoolean("imperial_units", false)) {
			filterDistancePreference.setSummary(R.string.pref_distance_summary_miles);
			filterDistancePreference.setDialogMessage(R.string.pref_distance_summary_miles);
		}
		
		final CheckBoxPreference imperialUnitsPreference = (CheckBoxPreference) findPreference("imperial_units");
		imperialUnitsPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				float distance = Float.parseFloat(filterDistancePreference.getText());
				if (((Boolean) newValue)) {
					filterDistancePreference.setText(Float.toString(distance / 1.609344F));
					filterDistancePreference.setSummary(R.string.pref_distance_summary_miles);
					filterDistancePreference.setDialogMessage(R.string.pref_distance_summary_miles);
				} else {
					filterDistancePreference.setText(Float.toString(distance * 1.609344F));
					filterDistancePreference.setSummary(R.string.pref_distance_summary_km);
					filterDistancePreference.setDialogMessage(R.string.pref_distance_summary_km);
				}
				return true;
			}
		});
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
			case R.id.selectAll:
				for (int i = 0; i < CacheType.values().length; i++)
					((CheckBoxPreference)findPreference("filter_" + i)).setChecked(true);
				return true;
			case R.id.deselectAll:
				for (int i = 0; i < CacheType.values().length; i++)
					((CheckBoxPreference)findPreference("filter_" + i)).setChecked(false);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
