package com.arcao.geocaching4locus;

import locus.api.android.ActionTools;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.RequiredVersionMissingException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class MenuActivity extends AbstractActionBarActivity {
	private final static String TAG = "G4L|MenuActivity";
	
	private SharedPreferences prefs;
	private ToggleButton liveMapButton; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.menu_dialog);

		findViewById(R.id.image_view_separator_setting).setVisibility(View.GONE);
		findViewById(R.id.header_preferences).setVisibility(View.GONE);
		
		applyMenuItemOnView(R.id.main_activity_option_menu_close, R.id.header_close);
		applyMenuItemOnView(R.id.main_activity_option_menu_preferences, R.id.header_preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		liveMapButton = (ToggleButton) findViewById(R.id.btn_menu_live_map);
	}

	@Override
	protected void onResume() {
		super.onResume();

		liveMapButton.setChecked(prefs.getBoolean(PrefConstants.LIVE_MAP, false));
	}
	
	public void onClickImportFromGC(View view) {
		startActivityForResult(new Intent(this, ImportFromGCActivity.class), 0);
	}

	public void onClickLiveMap(View view) {
		boolean activated = liveMapButton.isChecked();
		
		if (activated && !isPeriodicUpdateEnabled(this)) {
			activated = false;
			liveMapButton.setChecked(activated);

			Toast.makeText(this, getText(R.string.livemap_disabled), Toast.LENGTH_LONG).show();
		} else if (activated) {
			Toast.makeText(this, getText(R.string.livemap_activated), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, getText(R.string.livemap_deactivated), Toast.LENGTH_LONG).show();
		}

		prefs.edit().putBoolean(PrefConstants.LIVE_MAP, activated).commit();

		// hide dialog only when was started from Locus
		if (LocusUtils.isIntentMainFunction(getIntent())) {
			finish();
		}
	}

	public void onClickManual(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, AppConstants.MANUAL_URI));
	}

	public void onClickNearest(View view) {
		Intent intent = null;
		
		// copy intent data from Locus
		// FIX Android 2.3.3 can't start activity second time
		if (LocusUtils.isIntentMainFunction(getIntent())) {
			intent = new Intent(getIntent());
			intent.setClass(this, SearchNearestActivity.class);
		} else {
			intent = new Intent(this, SearchNearestActivity.class);
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
	
	public static boolean isPeriodicUpdateEnabled(Context ctx) {
		try {
			return ActionTools.isPeriodicUpdatesEnabled(ctx);
		} catch (RequiredVersionMissingException e) {
			Log.e(TAG, e.toString(), e);
			return false;
		}
	}
}
