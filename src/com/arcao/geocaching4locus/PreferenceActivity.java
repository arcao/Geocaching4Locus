package com.arcao.geocaching4locus;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

public class PreferenceActivity extends android.preference.PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		
		EditTextPreference filterDistancePreference = (EditTextPreference) findPreference("filter_distance");
		EditText filterDistanceEditText = (EditText)filterDistancePreference.getEditText(); 
		filterDistanceEditText.setKeyListener(DigitsKeyListener.getInstance(false,true));
	}
}
