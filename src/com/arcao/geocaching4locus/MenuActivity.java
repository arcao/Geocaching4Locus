package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ToggleButton;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.LiveMapNotificationManager;
import locus.api.android.utils.LocusUtils;

public class MenuActivity extends AbstractActionBarActivity implements LiveMapNotificationManager.LiveMapStateChangeListener {
	private ToggleButton liveMapButton;
	private LiveMapNotificationManager liveMapNotificationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.menu_dialog);

		findViewById(R.id.image_view_separator_setting).setVisibility(View.GONE);
		findViewById(R.id.header_preferences).setVisibility(View.GONE);

		applyMenuItemOnView(R.id.main_activity_option_menu_close, R.id.header_close);
		applyMenuItemOnView(R.id.main_activity_option_menu_preferences, R.id.header_preferences);

		liveMapNotificationManager = LiveMapNotificationManager.get(this);
		liveMapButton = (ToggleButton) findViewById(R.id.btn_menu_live_map);
	}

	@Override
	protected void onResume() {
		super.onResume();

		liveMapButton.setChecked(liveMapNotificationManager.isLiveMapEnabled());
		liveMapNotificationManager.addLiveMapStateChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		liveMapNotificationManager.removeLiveMapStateChangeListener(this);
	}

	public void onClickImportFromGC(View view) {
		startActivityForResult(new Intent(this, ImportFromGCActivity.class), 0);
	}

	public void onClickLiveMap(View view) {
		liveMapNotificationManager.setLiveMapEnabled(!liveMapNotificationManager.isLiveMapEnabled());
		liveMapButton.setChecked(liveMapNotificationManager.isLiveMapEnabled());

		// hide dialog only when was started from Locus
		if (LocusUtils.isIntentMainFunction(getIntent())) {
			finish();
		}
	}

	public void onClickManual(View view) {
		startActivity(new Intent(Intent.ACTION_VIEW, AppConstants.MANUAL_URI));
	}

	public void onClickNearest(View view) {
		Intent intent;

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

	@Override
	public void onLiveMapStateChange(boolean newState) {
		liveMapButton.setChecked(newState);
	}
}
