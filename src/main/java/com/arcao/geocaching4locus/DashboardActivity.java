package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;

import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.LiveMapNotificationManager;

import locus.api.android.utils.LocusUtils;

public class DashboardActivity extends AbstractActionBarActivity implements LiveMapNotificationManager.LiveMapStateChangeListener {
	private ToggleButton mLiveMapButton;
	private LiveMapNotificationManager mLiveMapNotificationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_dashboard);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setTitle(getTitle());

		mLiveMapNotificationManager = LiveMapNotificationManager.get(this);
		mLiveMapButton = (ToggleButton) findViewById(R.id.btn_menu_live_map);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mLiveMapButton.setChecked(mLiveMapNotificationManager.isLiveMapEnabled());
		mLiveMapNotificationManager.addLiveMapStateChangeListener(this);
	}

	@Override
	protected void onPause() {
		mLiveMapNotificationManager.removeLiveMapStateChangeListener(this);

		super.onPause();
	}

	public void onClickImportFromGC(View view) {
		startActivityForResult(new Intent(this, ImportFromGCActivity.class), 0);
	}

	public void onClickLiveMap(View view) {
		mLiveMapNotificationManager.setLiveMapEnabled(!mLiveMapNotificationManager.isLiveMapEnabled());
		mLiveMapButton.setChecked(mLiveMapNotificationManager.isLiveMapEnabled());

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
		startActivity(SettingsActivity.createIntent(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.toolbar_dashboard, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.main_activity_option_menu_preferences:
				startActivity(SettingsActivity.createIntent(this));
				return true;
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
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
		mLiveMapButton.setChecked(newState);
	}
}
