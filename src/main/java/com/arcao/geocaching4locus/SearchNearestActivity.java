package com.arcao.geocaching4locus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.fragment.dialog.DownloadNearestDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.LocationUpdateDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.NoLocationPermissionErrorDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.NoLocationProviderDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.SliderDialogFragment;
import com.arcao.geocaching4locus.fragment.preference.FilterPreferenceFragment;
import com.arcao.geocaching4locus.util.AnalyticsUtil;
import com.arcao.geocaching4locus.util.Coordinates;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.PermissionUtil;
import com.arcao.geocaching4locus.util.PreferenceUtil;
import com.arcao.geocaching4locus.util.SpannedFix;
import com.arcao.geocaching4locus.widget.SpinnerTextView;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.OnIntentMainFunction;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;
import org.apache.commons.lang3.StringUtils;
import timber.log.Timber;

public class SearchNearestActivity extends AbstractActionBarActivity implements LocationUpdateDialogFragment.DialogListener, OnIntentMainFunction, SliderDialogFragment.DialogListener, DownloadNearestDialogFragment.DialogListener {
  private static final String STATE_LATITUDE = "LATITUDE";
  private static final String STATE_LONGITUDE = "LONGITUDE";
  private static final String STATE_HAS_COORDINATES = "HAS_COORDINATES";

  private static final int REQUEST_SIGN_ON = 1;
  private static final int REQUEST_LOCATION_PERMISSION = 2;

  private SharedPreferences mPrefs;
  private LocationManager mLocationManager;

  private double mLatitude = Double.NaN;
  private double mLongitude = Double.NaN;
  private boolean mHasCoordinates = false;

  @Bind(R.id.toolbar) Toolbar mToolbar;
  @Bind(R.id.latitude) EditText mLatitudeEditText;
  @Bind(R.id.longitude) EditText mLongitudeEditText;
  @Bind(R.id.counter) SpinnerTextView mCounter;
  @Bind(R.id.fab) FloatingActionButton fab;

  private String mCoordinatesSource = null;
  private boolean mUseFilter = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    setContentView(R.layout.activity_search_nearest);
    ButterKnife.bind(this);

    setSupportActionBar(mToolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(getTitle());
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    prepareCounterLayout();

    mLatitude = mPrefs.getFloat(PrefConstants.LAST_LATITUDE, 0);
    mLongitude = mPrefs.getFloat(PrefConstants.LAST_LONGITUDE, 0);

    if (!LocusTesting.isLocusInstalled(this)) {
      updateCoordinates();
      LocusTesting.showLocusMissingError(this);
      return; // skip retrieving Waypoint, it can crash because of old Locus API
    }

    if (LocusUtils.isIntentPointTools(getIntent())) {
      try {
        Waypoint p = LocusUtils.handleIntentPointTools(this, getIntent());
        if (p != null) {
          onReceived(LocusUtils.createLocusVersion(this, getIntent()), p.getLocation(),
              p.getLocation());
        }
      } catch (RequiredVersionMissingException e) {
        Timber.e(e, e.getMessage());
      }
    }
    else if (getIntent().hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)) {
      onReceived(
          LocusUtils.createLocusVersion(this, getIntent()),
          LocusUtils.getLocationFromIntent(getIntent(), LocusConst.INTENT_EXTRA_LOCATION_GPS),
          LocusUtils.getLocationFromIntent(getIntent(), LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)
      );
    }
    else if (LocusUtils.isIntentSearchList(getIntent())) {
      LocusUtils.handleIntentSearchList(this, getIntent(), this);
    }

    if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_HAS_COORDINATES)) {
      mLatitude = savedInstanceState.getDouble(STATE_LATITUDE);
      mLongitude = savedInstanceState.getDouble(STATE_LONGITUDE);
      mHasCoordinates = true;
    }

    if (savedInstanceState != null) {
      fab.startAnimation(AnimationUtils.loadAnimation(this, R.anim.simple_grow));
    }

    updateCoordinates();

    if (!mHasCoordinates) {
      onGpsClick();
    }
  }

  @OnFocusChange({R.id.latitude, R.id.longitude})
  public void onCoordinateFocusChange(View v, boolean hasFocus) {
    if (hasFocus) return;

    mCoordinatesSource = AnalyticsUtil.COORDINATES_SOURCE_MANUAL;

    double deg = Coordinates.convertDegToDouble(((TextView) v).getText().toString());
    ((TextView) v).setText(Coordinates.convertDoubleToDeg(deg, v.getId() == R.id.longitude));
  }

  private void prepareCounterLayout() {
    int count = mPrefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT);
    final int step = PreferenceUtil.getParsedInt(mPrefs,
        PrefConstants.DOWNLOADING_COUNT_OF_CACHES_STEP,
        AppConstants.DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT);

    final int max = getMaxCountOfCaches();

    if (count > max) {
      count = max;
      mPrefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, count).apply();
    }

    if (count % step != 0) {
      count = ((count  / step) + 1) * step;
      mPrefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, count).apply();
    }

    mCounter.setText(String.valueOf(count));

    mCounter.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int count = mPrefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES,
                AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT);
        SliderDialogFragment fragment =
                SliderDialogFragment.newInstance(R.string.dialog_count_of_caches_title, 0, step, max,
                        count, step);
        fragment.show(getFragmentManager(), "COUNTER");
      }
    });
  }

  private int getMaxCountOfCaches() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || Runtime.getRuntime().maxMemory() <= AppConstants.LOW_MEMORY_THRESHOLD)
      return AppConstants.DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY;

    return AppConstants.DOWNLOADING_COUNT_OF_CACHES_MAX;
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putBoolean(STATE_HAS_COORDINATES, mHasCoordinates);
    if (mHasCoordinates) {
      outState.putDouble(STATE_LATITUDE, mLatitude);
      outState.putDouble(STATE_LONGITUDE, mLongitude);
    }
  }

  @OnClick(R.id.gps)
  public void onGpsClick() {
    mHasCoordinates = true;
    if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      if (PermissionUtil.hasPermission(this, PermissionUtil.PERMISSION_LOCATION_GPS)) {
        requestLocation();
      } else {
        ActivityCompat.requestPermissions(this, PermissionUtil.PERMISSION_LOCATION_GPS, REQUEST_LOCATION_PERMISSION);
      }
    } else {
      if (PermissionUtil.hasPermission(this, PermissionUtil.PERMISSION_LOCATION_WIFI)) {
        requestLocation();
      } else {
        ActivityCompat.requestPermissions(this, PermissionUtil.PERMISSION_LOCATION_WIFI, REQUEST_LOCATION_PERMISSION);
      }
    }
  }

  private void requestLocation() {
    LocationUpdateDialogFragment.newInstance().show(getFragmentManager(), LocationUpdateDialogFragment.FRAGMENT_TAG);
  }

  @OnClick(R.id.filter)
  public void onFilterClick() {
    mUseFilter = true;
    startActivity(SettingsActivity.createIntent(this, FilterPreferenceFragment.class));
  }

  @OnClick(R.id.fab)
  public void onDownloadClick() {
    // test if user is logged in
    if (App.get(this).getAuthenticatorHelper().requestSignOn(this, REQUEST_SIGN_ON)) {
      return;
    }

    Timber.i("Lat: " + mLatitudeEditText.getText()+ "; Lon: " + mLongitudeEditText.getText());

    mLatitude = Coordinates.convertDegToDouble(mLatitudeEditText.getText().toString());
    mLongitude = Coordinates.convertDegToDouble(mLongitudeEditText.getText().toString());

    if (Double.isNaN(mLatitude) || Double.isNaN(mLongitude)) {
      showError(R.string.wrong_coordinates, null);
      return;
    }

    mPrefs.edit()
        .putFloat(PrefConstants.LAST_LATITUDE, (float) mLatitude)
        .putFloat(PrefConstants.LAST_LONGITUDE, (float) mLongitude)
        .apply();

    int count = mPrefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES,
        AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT);
    AnalyticsUtil.actionSearchNearest(mCoordinatesSource, mUseFilter, count,
        App.get(this).getAuthenticatorHelper().getRestrictions().isPremiumMember());

    DownloadNearestDialogFragment.newInstance(mLatitude, mLongitude, count).show(
            getFragmentManager(), DownloadNearestDialogFragment.FRAGMENT_TAG);
  }

  private void showError(int errorResId, String additionalMessage) {
    if (isFinishing())
      return;

    new MaterialDialog.Builder(this)
        .content(SpannedFix.fromHtml(getString(errorResId, StringUtils.defaultString(additionalMessage))))
        .positiveText(R.string.ok_button)
        .show();
  }

  private void updateCoordinates() {
    if (Double.isNaN(mLatitude)) mLatitude = 0;
    if (Double.isNaN(mLongitude)) mLongitude = 0;

    mLatitudeEditText.setText(Coordinates.convertDoubleToDeg(mLatitude, false));
    mLongitudeEditText.setText(Coordinates.convertDoubleToDeg(mLongitude, true));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.toolbar_search_nearest, menu);
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
    super.onActivityResult(requestCode, resultCode, data);

    // restart download process after log in
    if (requestCode == REQUEST_SIGN_ON && resultCode == RESULT_OK) {
      onDownloadClick();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == REQUEST_LOCATION_PERMISSION) {
      if (PermissionUtil.verifyPermissions(grantResults)) {
        requestLocation();
      } else {
        NoLocationPermissionErrorDialogFragment.newInstance().show(getFragmentManager(), NoLocationPermissionErrorDialogFragment.FRAGMENT_TAG);
      }
    }
  }

  // ---------------- LocationUpdateDialogFragment.DialogListener methods ----------------
  @Override
  public void onLocationUpdate(Location location) {
    if (location == null) {
      NoLocationProviderDialogFragment.newInstance().show(getFragmentManager(), NoLocationProviderDialogFragment.FRAGMENT_TAG);
      return;
    }

    mLatitude = location.getLatitude();
    mLongitude = location.getLongitude();
    mHasCoordinates = true;

    mCoordinatesSource = AnalyticsUtil.COORDINATES_SOURCE_GPS;

    updateCoordinates();

    mPrefs.edit()
        .putFloat(PrefConstants.LAST_LATITUDE, (float) mLatitude)
        .putFloat(PrefConstants.LAST_LONGITUDE, (float) mLongitude)
        .apply();
  }

  // ---------------- OnIntentMainFunction methods ----------------
  @Override
  public void onReceived(LocusUtils.LocusVersion lv, locus.api.objects.extra.Location locGps, locus.api.objects.extra.Location locMapCenter) {
    mLatitude = locMapCenter.getLatitude();
    mLongitude = locMapCenter.getLongitude();
    mHasCoordinates = true;

    mCoordinatesSource = AnalyticsUtil.COORDINATES_SOURCE_LOCUS;
    Timber.i("Called from Locus: lat=" + mLatitude + "; lon=" + mLongitude);
  }

  @Override
  public void onFailed() {}

  // ---------------- SliderDialogFragment.DialogListener methods ----------------
  @Override
  public void onDialogClosed(SliderDialogFragment fragment) {
    int value = fragment.getValue();

    mCounter.setText(String.valueOf(value));
    mPrefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, value).apply();
  }

  // ------
  @Override
  public void onDownloadFinished(Intent intent) {
    if (intent != null) {
      if (intent.resolveActivity(getPackageManager()) != null) {
        setResult(RESULT_OK);
        finish();
        startActivity(intent);
      } else {
        Toast.makeText(this,
            "Unable to start Locus Map application. Is Locus Map application installed?", Toast.LENGTH_LONG).show();
      }
    }
  }

  @Override
  public void onDownloadError(Intent errorIntent) {
    startActivity(errorIntent);
  }
}