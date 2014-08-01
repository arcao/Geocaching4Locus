package com.arcao.geocaching4locus;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.arcao.fragment.number_chooser.NumberChooserDialogFragment;
import com.arcao.fragment.number_chooser.NumberChooserDialogFragment.OnNumberChooserDialogClosedListener;
import com.arcao.geocaching4locus.authentication.AuthenticatorActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.receiver.SearchNearestActivityBroadcastReceiver;
import com.arcao.geocaching4locus.service.SearchGeocacheService;
import com.arcao.geocaching4locus.task.LocationUpdateTask;
import com.arcao.geocaching4locus.task.LocationUpdateTask.LocationUpdate;
import com.arcao.geocaching4locus.util.Coordinates;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.arcao.geocaching4locus.util.SpannedFix;

import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.OnIntentMainFunction;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;

public class SearchNearestActivity extends AbstractActionBarActivity implements LocationUpdate, OnIntentMainFunction, OnNumberChooserDialogClosedListener {
	private static final String TAG = "G4L|MainActivity";
	private static final boolean HONEYCOMB = android.os.Build.VERSION.SDK_INT >= 11;

	private static String STATE_LATITUDE = "latitude";
	private static String STATE_LONGITUDE = "longitude";
	private static String STATE_HAS_COORDINATES = "has_coordinates";

	private static final int REQUEST_LOGIN = 1;

	private double latitude = Double.NaN;
	private double longitude = Double.NaN;
	private boolean hasCoordinates = false;

	private SharedPreferences prefs;

	private EditText latitudeEditText;
	private EditText longitudeEditText;
	private EditText countOfCachesEditText;
	private boolean locusInstalled = false;

	private SearchNearestActivityBroadcastReceiver broadcastReceiver;
	private LocationUpdateTask locationUpdateTask;

	private boolean startDownload = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		broadcastReceiver = new SearchNearestActivityBroadcastReceiver(this);
		hasCoordinates = false;

		setContentView(R.layout.main_activity);

		// hide close button in normal view
		if (!isFloatingWindow()) {
			findViewById(R.id.image_view_separator_close).setVisibility(View.GONE);
			findViewById(R.id.header_close).setVisibility(View.GONE);
		}

		applyMenuItemOnView(R.id.main_activity_option_menu_close, R.id.header_close);
		applyMenuItemOnView(R.id.main_activity_option_menu_preferences, R.id.header_preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		latitude = prefs.getFloat(PrefConstants.LAST_LATITUDE, 0);
		longitude = prefs.getFloat(PrefConstants.LAST_LONGITUDE, 0);

		if (!LocusTesting.isLocusInstalled(this)) {
			locusInstalled = false;
			return; // skip retrieving Waypoint, it can crash because of old Locus API
		}

		if (LocusUtils.isIntentPointTools(getIntent())) {
			Waypoint p = null;

			try {
				p = LocusUtils.handleIntentPointTools(this, getIntent());
			} catch (RequiredVersionMissingException e) {
				Log.e(TAG, e.getMessage(), e);
			}

			if (p == null) {
				Toast.makeText(this, "Wrong INTENT - no point!", Toast.LENGTH_SHORT).show();
			} else {
				latitude = p.getLocation().getLatitude();
				longitude = p.getLocation().getLongitude();
				Log.i(TAG, "Called from Locus: lat=" + latitude + "; lon=" + longitude);

				hasCoordinates = true;
			}
		}
		else if (LocusUtils.isIntentMainFunction(getIntent())) {
			LocusUtils.handleIntentMainFunction(this, getIntent(), this);
		}
		else if (LocusUtils.isIntentSearchList(getIntent())) {
			LocusUtils.handleIntentSearchList(this, getIntent(), this);
		}

		if (savedInstanceState != null && savedInstanceState.getBoolean(STATE_HAS_COORDINATES)) {
			latitude = savedInstanceState.getDouble(STATE_LATITUDE);
			longitude = savedInstanceState.getDouble(STATE_LONGITUDE);
			hasCoordinates = true;
		}

		if (SearchGeocacheService.getInstance() != null && !SearchGeocacheService.getInstance().isCanceled()) {
			hasCoordinates = true;
		}
	}

	@Override
	public void onReceived(LocusUtils.LocusVersion lv, locus.api.objects.extra.Location locGps, locus.api.objects.extra.Location locMapCenter) {
		latitude = locMapCenter.getLatitude();
		longitude = locMapCenter.getLongitude();

		Log.i(TAG, "Called from Locus: lat=" + latitude + "; lon=" + longitude);

		hasCoordinates = true;
	}

	@Override
	public void onFailed() {}

	@Override
	protected void onResume() {
		super.onResume();

		latitudeEditText = (EditText) findViewById(R.id.latitudeEditText);
		longitudeEditText = (EditText) findViewById(R.id.logitudeEditText);
		countOfCachesEditText = (EditText) findViewById(R.id.cacheCountEditText);

		latitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					double deg = Coordinates.convertDegToDouble(latitudeEditText.getText().toString());
					if (Double.isNaN(deg)) {
						((EditText)v).setText("N/A");
					} else {
						((EditText)v).setText(Coordinates.convertDoubleToDeg(deg, false));
					}
				}
			}
		});

		longitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					double deg = Coordinates.convertDegToDouble(longitudeEditText.getText().toString());
					if (Double.isNaN(deg)) {
						((EditText)v).setText("N/A");
					} else {
						((EditText)v).setText(Coordinates.convertDoubleToDeg(deg, true));
					}
				}
			}
		});

		int countOfCaches = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT);
		int max = getMaxCountOfCaches();

		if (countOfCaches > max) {
			countOfCaches = max;
			prefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, countOfCaches).commit();
		}

		if (countOfCaches % AppConstants.DOWNLOADING_COUNT_OF_CACHES_STEP != 0) {
			countOfCaches = ((countOfCaches  / AppConstants.DOWNLOADING_COUNT_OF_CACHES_STEP) + 1) * AppConstants.DOWNLOADING_COUNT_OF_CACHES_STEP;
			prefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, countOfCaches).commit();
		}

		countOfCachesEditText.setText(String.valueOf(prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT)));
		countOfCachesEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int countOfCaches = prefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT);
				NumberChooserDialogFragment fragment = NumberChooserDialogFragment.newInstance(R.string.dialog_count_of_caches_title, R.plurals.plurals_cache,
								AppConstants.DOWNLOADING_COUNT_OF_CACHES_MIN, getMaxCountOfCaches(), countOfCaches, AppConstants.DOWNLOADING_COUNT_OF_CACHES_STEP);
				fragment.show(getSupportFragmentManager(), "countOfCaches");
			}
		});
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();

		// play with fragments here
		broadcastReceiver.register(this);

		if (!locusInstalled && !LocusTesting.isLocusInstalled(this)) {
			locusInstalled = false;
			LocusTesting.showLocusMissingError(this);
			return;
		}

		locusInstalled = true;

		if (startDownload) {
			startDownload = false;
			download();
		} else if (!hasCoordinates) {
			acquireCoordinates();
		} else {
			updateCoordinateTextView();
			requestProgressUpdate();
		}

	}

	@Override
	public void onNumberChooserDialogClosed(NumberChooserDialogFragment fragment) {
		int value = fragment.getValue();

		countOfCachesEditText.setText(String.valueOf(value));
		prefs.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, value).commit();
	}

	@Override
	protected void onPause() {
		if (locationUpdateTask != null)
			locationUpdateTask.detach();

		broadcastReceiver.unregister(this);

		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Fragments can't be used after onSaveInstanceState on Honeycomb+
		if (HONEYCOMB) {
			if (locationUpdateTask != null)
				locationUpdateTask.detach();

			broadcastReceiver.unregister(this);
		}

		super.onSaveInstanceState(outState);

		outState.putBoolean(STATE_HAS_COORDINATES, hasCoordinates);
		if (hasCoordinates) {
			outState.putDouble(STATE_LATITUDE, latitude);
			outState.putDouble(STATE_LONGITUDE, longitude);
		}
	}

	public void onClickSearch(View view) {
		download();
	}

	public void onClickGps(View view) {
		acquireCoordinates();
	}

	public void onClickFilter(View view) {
		startActivity(new Intent(this, PreferenceActivity.class)
						.putExtra(PreferenceActivity.PARAM_SCREEN, PreferenceActivity.PARAM_SCREEN__FILTERS));
	}

	public void onClickClose(View view) {
		finish();
	}

	public void onClickSettings(View view) {
		startActivity(new Intent(this, PreferenceActivity.class));
	}

	protected void download() {
		// test if user is logged in
		if (!Geocaching4LocusApplication.getAuthenticatorHelper().hasAccount()) {
			startActivityForResult(AuthenticatorActivity.createIntent(this, true), REQUEST_LOGIN);
			return;
		}

		Log.i(TAG, "Lat: " + latitudeEditText.getText().toString() + "; Lon: " + longitudeEditText.getText().toString());

		latitude = Coordinates.convertDegToDouble(latitudeEditText.getText().toString());
		longitude = Coordinates.convertDegToDouble(longitudeEditText.getText().toString());

		if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
			showError(R.string.wrong_coordinates, null);
		}

		Editor editor = prefs.edit();
		editor.putFloat(PrefConstants.LAST_LATITUDE, (float) latitude);
		editor.putFloat(PrefConstants.LAST_LONGITUDE, (float) longitude);
		editor.commit();

		Intent intent = new Intent(this, SearchGeocacheService.class);
		intent.putExtra(SearchGeocacheService.PARAM_LATITUDE, latitude);
		intent.putExtra(SearchGeocacheService.PARAM_LONGITUDE, longitude);
		startService(intent);
	}

	protected void showError(int errorResId, String additionalMessage) {
		if (isFinishing())
			return;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = String.format(getString(errorResId), additionalMessage);

		builder.setMessage(SpannedFix.fromHtml(message));
		builder.setTitle(R.string.error_title);
		builder.setPositiveButton(R.string.ok_button, null);
		builder.show();
	}

	protected void acquireCoordinates() {
		// search location
		// Acquire a reference to the system Location Manager
		locationUpdateTask = new LocationUpdateTask(this);
		locationUpdateTask.execute();
	}

	private void updateCoordinateTextView() {
		if (latitudeEditText != null)
			latitudeEditText.setText(Coordinates.convertDoubleToDeg(latitude, false));
		if (longitudeEditText != null)
			longitudeEditText.setText(Coordinates.convertDoubleToDeg(longitude, true));
	}

	@Override
	public void onLocationUpdate(Location location) {
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		hasCoordinates = true;

		updateCoordinateTextView();

		Editor editor = prefs.edit();
		editor.putFloat(PrefConstants.LAST_LATITUDE, (float) latitude);
		editor.putFloat(PrefConstants.LAST_LONGITUDE, (float) longitude);
		editor.commit();
	}

	protected void requestProgressUpdate() {
        if (SearchGeocacheService.getInstance() != null && !SearchGeocacheService.getInstance().isCanceled()) {
			SearchGeocacheService.getInstance().sendProgressUpdate();
        }
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
		// restart download process after log in
		if (requestCode == REQUEST_LOGIN && resultCode == RESULT_OK) {
			// do not call download method directly here, must be called in onResume method
			startDownload = true;
		}
	}

	protected int getMaxCountOfCaches() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || Runtime.getRuntime().maxMemory() <= AppConstants.LOW_MEMORY_THRESHOLD)
			return AppConstants.DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY;

		return AppConstants.DOWNLOADING_COUNT_OF_CACHES_MAX;
	}
}