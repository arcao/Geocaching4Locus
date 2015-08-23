package com.arcao.geocaching4locus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.IntentUtil;
import com.arcao.geocaching4locus.util.LiveMapNotificationManager;
import com.arcao.geocaching4locus.widget.DashboardButton;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;

public class DashboardActivity extends AbstractActionBarActivity implements LiveMapNotificationManager.LiveMapStateChangeListener {
	private LiveMapNotificationManager mLiveMapNotificationManager;

	@Bind(R.id.db_live_map) DashboardButton mLiveMapButton;
	@Bind(R.id.db_import_bookmark) DashboardButton mImportBookmarkButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_dashboard);
    ButterKnife.bind(this);

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		getSupportActionBar().setTitle(getTitle());

		mLiveMapNotificationManager = LiveMapNotificationManager.get(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mLiveMapButton.setChecked(mLiveMapNotificationManager.isLiveMapEnabled());
		mLiveMapNotificationManager.addLiveMapStateChangeListener(this);
    
    mImportBookmarkButton.setEnabled(App.get(this).getAuthenticatorHelper().getRestrictions().isPremiumMember());
	}

	@Override
	protected void onPause() {
		mLiveMapNotificationManager.removeLiveMapStateChangeListener(this);

		super.onPause();
	}

	public void onClickImportFromGC(View view) {
		startActivityForResult(new Intent(this, ImportFromGCActivity.class), 0);
	}

  @OnClick(R.id.db_live_map)
	public void onClickLiveMap() {
		mLiveMapNotificationManager.setLiveMapEnabled(!mLiveMapNotificationManager.isLiveMapEnabled());
		mLiveMapButton.setChecked(mLiveMapNotificationManager.isLiveMapEnabled());

		// hide dialog only when was started from Locus
		if (LocusUtils.isIntentMainFunction(getIntent()) || LocusUtils.isIntentMainFunctionGc(getIntent())) {
			finish();
		}
	}

	public void onClickManual(View view) {
		IntentUtil.showWebPage(this, AppConstants.MANUAL_URI);
	}

	public void onClickNearest(View view) {
		Intent intent;

		// copy intent data from Locus
		// FIX Android 2.3.3 can't start activity second time
		if (getIntent().hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)) {
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

  @OnClick(R.id.db_import_bookmark)
	public void onClickImportBookmark() {
		startActivity(new Intent(this, ImportBookmarkActivity.class));
		finish();
	}
}
