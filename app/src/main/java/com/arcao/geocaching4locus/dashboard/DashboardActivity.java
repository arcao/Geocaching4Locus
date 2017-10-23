package com.arcao.geocaching4locus.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.AbstractActionBarActivity;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.util.AnalyticsUtil;
import com.arcao.geocaching4locus.base.util.IntentUtil;
import com.arcao.geocaching4locus.base.util.LocusTesting;
import com.arcao.geocaching4locus.dashboard.widget.DashboardButton;
import com.arcao.geocaching4locus.download_rectangle.DownloadRectangleActivity;
import com.arcao.geocaching4locus.import_bookmarks.ImportBookmarkActivity;
import com.arcao.geocaching4locus.import_gc.ImportFromGCActivity;
import com.arcao.geocaching4locus.live_map.model.LastLiveMapData;
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager;
import com.arcao.geocaching4locus.search_nearest.SearchNearestActivity;
import com.arcao.geocaching4locus.settings.SettingsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;

public class DashboardActivity extends AbstractActionBarActivity implements LiveMapNotificationManager.LiveMapStateChangeListener {
	private static final int REQUEST_SIGN_ON = 1;

	private LiveMapNotificationManager mLiveMapNotificationManager;
	private boolean mCalledFromLocus;

	@BindView(R.id.db_live_map) DashboardButton mLiveMapButton;
	@BindView(R.id.db_import_bookmark) DashboardButton mImportBookmarkButton;
	@BindView(R.id.db_live_map_download_caches) DashboardButton mLiveMapDownloadCachesButton;
	@BindView(R.id.toolbar) Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLiveMapNotificationManager = LiveMapNotificationManager.get(this);
		mCalledFromLocus = LocusUtils.isIntentMainFunction(getIntent()) || LocusUtils.isIntentMainFunctionGc(getIntent()) ||
				getIntent().hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER);

		setContentView(R.layout.activity_dashboard);
		ButterKnife.bind(this);

		setSupportActionBar(toolbar);
		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle(getTitle());
		}

		AnalyticsUtil.actionDashboard(mCalledFromLocus);
	}

	@Override
	protected void onResume() {
		super.onResume();
		boolean premiumMember = App.get(this).getAccountManager().isPremium();

		mLiveMapNotificationManager.addLiveMapStateChangeListener(this);

		mLiveMapButton.setChecked(mLiveMapNotificationManager.isLiveMapEnabled());
		mImportBookmarkButton.setEnabled(premiumMember);
		if (!premiumMember) {
			mImportBookmarkButton.setText(String.format("%s %s", getString(R.string.menu_import_bookmarks), AppConstants.PREMIUM_CHARACTER));
		} else {
			mImportBookmarkButton.setText(R.string.menu_import_bookmarks);
		}

		mLiveMapDownloadCachesButton.setEnabled(LastLiveMapData.getInstance().isValid());
	}

	@Override
	protected void onPause() {
		mLiveMapNotificationManager.removeLiveMapStateChangeListener(this);

		super.onPause();
	}

	@OnClick(R.id.db_live_map)
	public void onClickLiveMap() {
		// test if Locus Map is installed
		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}

		// test if user is logged in
		if (App.get(this).getAccountManager().requestSignOn(this, REQUEST_SIGN_ON)) {
			return;
		}

		mLiveMapNotificationManager.setLiveMapEnabled(!mLiveMapNotificationManager.isLiveMapEnabled());
		mLiveMapButton.setChecked(mLiveMapNotificationManager.isLiveMapEnabled());

		// hide dialog only when was started from Locus
		if (mCalledFromLocus) {
			finish();
		}
	}

	@OnClick(R.id.db_live_map_download_caches)
	public void onClickLiveMapDownloadCaches() {
		startActivityForResult(new Intent(this, DownloadRectangleActivity.class), 0);
	}

	@OnClick(R.id.db_import_gc)
	public void onClickImportGC() {
		startActivityForResult(new Intent(this, ImportFromGCActivity.class), 0);
	}

	@OnClick(R.id.db_search_nearest)
	public void onClickSearchNearest() {
		Intent intent;

		// copy intent data from Locus
		// FIX Android 2.3.3 can't start activity second time
		if (getIntent().hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)) {
			intent = new Intent(getIntent());
			intent.setClass(this, SearchNearestActivity.class);
		} else {
			intent = new Intent(this, SearchNearestActivity.class);
		}

		startActivityForResult(intent, 0);
	}

	@OnClick(R.id.db_import_bookmark)
	public void onClickImportBookmark() {
		startActivityForResult(new Intent(this, ImportBookmarkActivity.class), 0);
	}

	@OnClick(R.id.db_preferences)
	public void onClickPreferences() {
		startActivity(SettingsActivity.createIntent(this));
	}

	@OnClick(R.id.db_manual)
	public void onClickManual() {
		IntentUtil.showWebPage(this, AppConstants.MANUAL_URI);
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
				onClickPreferences();
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
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_SIGN_ON) {
			if (resultCode == RESULT_OK) {
				onClickLiveMap();
			}
			return;
		}

		if (resultCode == RESULT_OK) {
			finish();
		}
	}

	@Override
	public void onLiveMapStateChange(boolean newState) {
		mLiveMapButton.setChecked(newState);
	}
}
