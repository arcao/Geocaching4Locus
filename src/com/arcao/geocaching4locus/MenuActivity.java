package com.arcao.geocaching4locus;

import locus.api.android.utils.LocusUtils;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class MenuActivity extends AbstractActionBarActivity {
	private SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.menu_dialog);

		findViewById(R.id.image_view_separator_setting).setVisibility(View.GONE);
		findViewById(R.id.header_preferences).setVisibility(View.GONE);
		
		applyMenuItemOnView(R.id.main_activity_option_menu_close, R.id.header_close);
		applyMenuItemOnView(R.id.main_activity_option_menu_preferences, R.id.header_preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		ToggleButton liveMapButton = (ToggleButton) findViewById(R.id.btn_menu_live_map);
		liveMapButton.setChecked(prefs.getBoolean(PrefConstants.LIVE_MAP, false));
	}
	
	public void onClickImportFromGC(View view) {
		startActivityForResult(new Intent(this, ImportFromGCActivity.class), 0);
	}

	public void onClickLiveMap(View view) {
		boolean activated = !prefs.getBoolean(PrefConstants.LIVE_MAP, false);
		prefs.edit().putBoolean(PrefConstants.LIVE_MAP, activated).commit();

		if (activated) {
			Toast.makeText(this, getText(R.string.livemap_activated), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, getText(R.string.livemap_deactivated), Toast.LENGTH_LONG).show();
		}

		finish();
	}

	public void onClickManual(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, AppConstants.MANUAL_URI));
		finish();
	}

	public void onClickNearest(View view) {
		Intent intent = null;
		
		// copy intent data from Locus
		// FIX Android 2.3.3 can't start activity second time
		if (LocusUtils.isIntentMainFunction(getIntent())) {
			intent = new Intent(getIntent());
			intent.setClass(this, MainActivity.class);
		} else {
			intent = new Intent(this, MainActivity.class);
		}

		startActivity(intent);
		finish();
	}
	
	public void onClickPreferences(View view) {
		startActivity(new Intent(this, PreferenceActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(int itemId) {
		switch (itemId) {
			case R.id.main_activity_option_menu_preferences:
				startActivity(new Intent(this, PreferenceActivity.class));
				return true;
			case R.id.main_activity_option_menu_close:
			case android.R.id.home:
				finish();
				return true;
			default:
				return false;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			finish();
		}
	}
}
