package com.arcao.geocaching4locus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.afollestad.materialdialogs.MaterialDialog;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.fragment.dialog.NoLocationPermissionErrorDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.SliderDialogFragment;
import com.arcao.geocaching4locus.fragment.preference.FilterPreferenceFragment;
import com.arcao.geocaching4locus.receiver.SearchNearestActivityBroadcastReceiver;
import com.arcao.geocaching4locus.service.SearchGeocacheService;
import com.arcao.geocaching4locus.task.LocationUpdateTask;
import com.arcao.geocaching4locus.task.LocationUpdateTask.LocationUpdate;
import com.arcao.geocaching4locus.util.Coordinates;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.PermissionUtil;
import com.arcao.geocaching4locus.util.PreferenceUtil;
import com.arcao.geocaching4locus.util.SpannedFix;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.OnIntentMainFunction;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;
import org.apache.commons.lang3.StringUtils;
import timber.log.Timber;

public class SearchNearestActivity extends AbstractActionBarActivity implements LocationUpdate, OnIntentMainFunction, SliderDialogFragment.DialogListener {
  private static final String STATE_LATITUDE = "latitude";
  private static final String STATE_LONGITUDE = "longitude";
  private static final String STATE_HAS_COORDINATES = "has_coordinates";

  private static final int REQUEST_LOGIN = 1;
  private static final int REQUEST_LOCATION_PERMISSION = 2;

  private SharedPreferences mPrefs;
  private SearchNearestActivityBroadcastReceiver mBroadcastReceiver;
  private LocationUpdateTask mTask;

  private double mLatitude = Double.NaN;
  private double mLongitude = Double.NaN;

  @Bind(R.id.toolbar) Toolbar toolbar;
  @Bind(R.id.latitudeEditText) EditText mLatitudeEditText;
  @Bind(R.id.logitudeEditText) EditText mLongitudeEditText;
  @Bind(R.id.cacheCountEditText) EditText mCountOfCachesEditText;

  private boolean mHasCoordinates = false;
  private boolean mLocusInstalled = false;
  private boolean mStartDownload = false;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
    mBroadcastReceiver = new SearchNearestActivityBroadcastReceiver(this);

    setContentView(R.layout.activity_search_nearest);
    ButterKnife.bind(this);

    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle(getTitle());
    }

    mLatitude = mPrefs.getFloat(PrefConstants.LAST_LATITUDE, 0);
    mLongitude = mPrefs.getFloat(PrefConstants.LAST_LONGITUDE, 0);

    if (!LocusTesting.isLocusInstalled(this)) {
      mLocusInstalled = false;
      return; // skip retrieving Waypoint, it can crash because of old Locus API
    }

    if (LocusUtils.isIntentPointTools(getIntent())) {
      try {
        Waypoint p = LocusUtils.handleIntentPointTools(this, getIntent());
        if (p == null) {
          Toast.makeText(this, "Wrong INTENT - no point!", Toast.LENGTH_SHORT).show();
        } else {
          mLatitude = p.getLocation().getLatitude();
          mLongitude = p.getLocation().getLongitude();
          Timber.i("Called from Locus: lat=" + mLatitude + "; lon=" + mLongitude);

          mHasCoordinates = true;
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

    if (SearchGeocacheService.getInstance() != null && !SearchGeocacheService.getInstance().isCanceled()) {
      mHasCoordinates = true;
    }
  }

  @Override
  public void onReceived(LocusUtils.LocusVersion lv, locus.api.objects.extra.Location locGps, locus.api.objects.extra.Location locMapCenter) {
    mLatitude = locMapCenter.getLatitude();
    mLongitude = locMapCenter.getLongitude();
    mHasCoordinates = true;

    Timber.i("Called from Locus: lat=" + mLatitude + "; lon=" + mLongitude);
  }

  @Override
  public void onFailed() {}

  @Override
  protected void onResume() {
    super.onResume();

    mLatitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          double deg = Coordinates.convertDegToDouble(mLatitudeEditText.getText().toString());
          if (Double.isNaN(deg)) {
            ((TextView) v).setText("N/A");
          } else {
            ((TextView) v).setText(Coordinates.convertDoubleToDeg(deg, false));
          }
        }
      }
    });

    mLongitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          double deg = Coordinates.convertDegToDouble(mLongitudeEditText.getText().toString());
          if (Double.isNaN(deg)) {
            ((TextView) v).setText("N/A");
          } else {
            ((TextView) v).setText(Coordinates.convertDoubleToDeg(deg, true));
          }
        }
      }
    });

    int countOfCaches = mPrefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT);
    final int countOfCachesStep = PreferenceUtil.getParsedInt(mPrefs, PrefConstants.DOWNLOADING_COUNT_OF_CACHES_STEP, AppConstants.DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT);

    int max = getMaxCountOfCaches();

    if (countOfCaches > max) {
      countOfCaches = max;
      mPrefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, countOfCaches).apply();
    }

    if (countOfCaches % countOfCachesStep != 0) {
      countOfCaches = ((countOfCaches  / countOfCachesStep) + 1) * countOfCachesStep;
      mPrefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, countOfCaches).apply();
    }

    mCountOfCachesEditText.setText(String.valueOf(mPrefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT)));
    mCountOfCachesEditText.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        int countOfCaches = mPrefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT);
        SliderDialogFragment fragment = SliderDialogFragment.newInstance(R.string.dialog_count_of_caches_title, 0,
            countOfCachesStep, getMaxCountOfCaches(), countOfCaches, countOfCachesStep);
        fragment.show(getFragmentManager(), "countOfCaches");
      }
    });
  }

  @Override
  protected void onResumeFragments() {
    super.onResumeFragments();

    // play with fragments here
    mBroadcastReceiver.register(this);

    if (!mLocusInstalled && !LocusTesting.isLocusInstalled(this)) {
      mLocusInstalled = false;
      LocusTesting.showLocusMissingError(this);
      return;
    }

    mLocusInstalled = true;

    if (mStartDownload) {
      mStartDownload = false;
      download();
    } else if (!mHasCoordinates) {
      requestCoordinates();
    } else {
      updateCoordinateTextView();
      requestProgressUpdate();
    }

  }

  @Override
  public void onDialogClosed(SliderDialogFragment fragment) {
    int value = fragment.getValue();

    mCountOfCachesEditText.setText(String.valueOf(value));
    mPrefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, value).apply();
  }

  @Override
  protected void onPause() {
    if (mTask != null)
      mTask.detach();

    mBroadcastReceiver.unregister(this);

    super.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    // Fragments can't be used after onSaveInstanceState
    if (mTask != null)
      mTask.detach();

    mBroadcastReceiver.unregister(this);

    super.onSaveInstanceState(outState);

    outState.putBoolean(STATE_HAS_COORDINATES, mHasCoordinates);
    if (mHasCoordinates) {
      outState.putDouble(STATE_LATITUDE, mLatitude);
      outState.putDouble(STATE_LONGITUDE, mLongitude);
    }
  }

  public void onClickSearch(View view) {
    download();
  }

  public void onClickGps(View view) {
    requestCoordinates();
  }

  public void onClickFilter(View view) {
    startActivity(SettingsActivity.createIntent(this, FilterPreferenceFragment.class));
  }

  private void download() {
    // test if user is logged in
    if (!App.get(this).getAuthenticatorHelper().isLoggedIn(this, REQUEST_LOGIN)) {
      return;
    }

    Timber.i("Lat: " + mLatitudeEditText.getText().toString() + "; Lon: " + mLongitudeEditText.getText().toString());

    mLatitude = Coordinates.convertDegToDouble(mLatitudeEditText.getText().toString());
    mLongitude = Coordinates.convertDegToDouble(mLongitudeEditText.getText().toString());

    if (Double.isNaN(mLatitude) || Double.isNaN(mLongitude)) {
      showError(R.string.wrong_coordinates, null);
    }

    mPrefs.edit()
        .putFloat(PrefConstants.LAST_LATITUDE, (float) mLatitude)
        .putFloat(PrefConstants.LAST_LONGITUDE, (float) mLongitude)
        .apply();

    Intent intent = new Intent(this, SearchGeocacheService.class);
    intent.putExtra(SearchGeocacheService.PARAM_LATITUDE, mLatitude);
    intent.putExtra(SearchGeocacheService.PARAM_LONGITUDE, mLongitude);
    startService(intent);
  }

  private void showError(int errorResId, String additionalMessage) {
    if (isFinishing())
      return;

    new MaterialDialog.Builder(this)
        .title(R.string.error_title)
        .content(SpannedFix.fromHtml(getString(errorResId, StringUtils.defaultString(additionalMessage))))
        .positiveText(R.string.ok_button)
        .show();
  }


  private void requestCoordinates() {
    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    String[] permissions;

    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
      permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    } else {
      permissions = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    ActivityCompat.requestPermissions(this, permissions, REQUEST_LOCATION_PERMISSION);
  }

  private void acquireCoordinates() {
    // search location
    // Acquire a reference to the system Location Manager
    mTask = new LocationUpdateTask(this);
    mTask.execute();
  }

  private void updateCoordinateTextView() {
    if (mLatitudeEditText != null)
      mLatitudeEditText.setText(Coordinates.convertDoubleToDeg(mLatitude, false));

    if (mLongitudeEditText != null)
      mLongitudeEditText.setText(Coordinates.convertDoubleToDeg(mLongitude, true));
  }

  @Override
  public void onLocationUpdate(Location location) {
    mLatitude = location.getLatitude();
    mLongitude = location.getLongitude();
    mHasCoordinates = true;

    updateCoordinateTextView();

    mPrefs.edit()
        .putFloat(PrefConstants.LAST_LATITUDE, (float) mLatitude)
        .putFloat(PrefConstants.LAST_LONGITUDE, (float) mLongitude)
        .apply();
  }

  private void requestProgressUpdate() {
    SearchGeocacheService service = SearchGeocacheService.getInstance();

    if (service != null && !service.isCanceled()) {
      service.sendProgressUpdate();
    }
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
    if (requestCode == REQUEST_LOGIN && resultCode == RESULT_OK) {
      // do not call download method directly here, must be called in onResume method
      mStartDownload = true;
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode == REQUEST_LOCATION_PERMISSION) {
      if (PermissionUtil.verifyPermissions(grantResults)) {
        acquireCoordinates();
      } else {
        NoLocationPermissionErrorDialogFragment.newInstance().show(getFragmentManager(), NoLocationPermissionErrorDialogFragment.FRAGMENT_TAG);
      }
    }
  }

  private int getMaxCountOfCaches() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || Runtime.getRuntime().maxMemory() <= AppConstants.LOW_MEMORY_THRESHOLD)
      return AppConstants.DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY;

    return AppConstants.DOWNLOADING_COUNT_OF_CACHES_MAX;
  }
}