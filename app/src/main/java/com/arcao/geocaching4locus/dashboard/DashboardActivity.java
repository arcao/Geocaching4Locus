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
import com.arcao.geocaching4locus.base.util.LocusMapUtil;
import com.arcao.geocaching4locus.dashboard.widget.DashboardButton;
import com.arcao.geocaching4locus.download_rectangle.DownloadRectangleActivity;
import com.arcao.geocaching4locus.import_bookmarks.ImportBookmarkActivity;
import com.arcao.geocaching4locus.import_gc.ImportFromGCActivity;
import com.arcao.geocaching4locus.live_map.fragment.PowerSaveWarningDialogFragment;
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager;
import com.arcao.geocaching4locus.search_nearest.SearchNearestActivity;
import com.arcao.geocaching4locus.settings.SettingsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;

public class DashboardActivity extends AbstractActionBarActivity implements LiveMapNotificationManager.LiveMapStateChangeListener, PowerSaveWarningDialogFragment.OnPowerSaveWarningConfirmedListener {
    private static final int REQUEST_SIGN_ON = 1;

    private LiveMapNotificationManager notificationManager;
    private boolean calledFromLocus;

    @BindView(R.id.db_live_map) DashboardButton liveMapButtonView;
    @BindView(R.id.db_import_bookmark) DashboardButton importBookmarkButtonView;
    @BindView(R.id.db_live_map_download_caches) DashboardButton liveMapDownloadCachesButtonView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationManager = LiveMapNotificationManager.get(this);
        calledFromLocus = LocusUtils.isIntentMainFunction(getIntent()) || LocusUtils.isIntentMainFunctionGc(getIntent()) ||
                getIntent().hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER);

        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getTitle());
        }

        AnalyticsUtil.actionDashboard(calledFromLocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean premiumMember = App.get(this).getAccountManager().isPremium();

        notificationManager.addLiveMapStateChangeListener(this);

        boolean liveMapEnabled = notificationManager.isLiveMapEnabled();

        liveMapButtonView.setChecked(liveMapEnabled);
        liveMapDownloadCachesButtonView.setEnabled(liveMapEnabled);

        importBookmarkButtonView.setEnabled(premiumMember);
        if (!premiumMember) {
            importBookmarkButtonView.setText(String.format("%s %s", getString(R.string.menu_import_bookmarks), AppConstants.PREMIUM_CHARACTER));
        } else {
            importBookmarkButtonView.setText(R.string.menu_import_bookmarks);
        }
    }

    @Override
    protected void onPause() {
        notificationManager.removeLiveMapStateChangeListener(this);

        super.onPause();
    }

    @OnClick(R.id.db_live_map)
    public void onClickLiveMap() {
        // test if Locus Map is installed
        if (LocusMapUtil.isLocusNotInstalled(this)) {
            LocusMapUtil.showLocusMissingError(this);
            return;
        }

        // test if user is logged in
        if (App.get(this).getAccountManager().requestSignOn(this, REQUEST_SIGN_ON)) {
            return;
        }

        if (notificationManager.isLiveMapEnabled() || !PowerSaveWarningDialogFragment.showIfNeeded(this)) {
            onPowerSaveWarningConfirmed();
        }
    }

    @Override
    public void onPowerSaveWarningConfirmed() {
        notificationManager.setLiveMapEnabled(!notificationManager.isLiveMapEnabled());

        boolean enabled = notificationManager.isLiveMapEnabled();
        liveMapButtonView.setChecked(enabled);
        liveMapDownloadCachesButtonView.setEnabled(enabled);

        // hide dialog only when was started from Locus
        if (calledFromLocus) {
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

    @OnClick(R.id.db_users_guide)
    public void onClickManual() {
        IntentUtil.showWebPage(this, AppConstants.USERS_GUIDE_URI);
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
        liveMapButtonView.setChecked(newState);
    }
}
