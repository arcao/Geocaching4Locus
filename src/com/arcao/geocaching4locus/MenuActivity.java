package com.arcao.geocaching4locus;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ToggleButton;

import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class MenuActivity extends Activity {
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.menu_dialog);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		ToggleButton liveMapButton = (ToggleButton) findViewById(R.id.btn_menu_live_map);
		liveMapButton.setChecked(prefs.getBoolean(PrefConstants.LIVE_MAP, false));
	}
	
	public void onClickClose(View view) {
		finish();
	}
	
	public void onClickSettings(View view) {
		startActivity(new Intent(this, PreferenceActivity.class));
	}
	
	public void onClickLiveMap(View view) {
		boolean activated = !prefs.getBoolean(PrefConstants.LIVE_MAP, false);
		prefs.edit().putBoolean(PrefConstants.LIVE_MAP, activated).commit();
		
		((ToggleButton)view).setChecked(activated);
	}
	
	public void onClickManual(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, AppConstants.MANUAL_URI));
		finish();
	}
	
	public void onClickNearest(View view) {
		// copy intent data from Locus
		Intent intent = new Intent(getIntent());
		intent.setClass(this, MainActivity.class);
		
		startActivity(intent);
		finish();
	}
}
