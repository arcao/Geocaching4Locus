package com.arcao.geocaching4locus;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

public class PreferenceActivity extends android.preference.PreferenceActivity {
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		
		final EditTextPreference filterDistancePreference = (EditTextPreference) findPreference("filter_distance");
		final EditText filterDistanceEditText = (EditText)filterDistancePreference.getEditText(); 
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
}
