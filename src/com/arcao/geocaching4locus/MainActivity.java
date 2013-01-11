package com.arcao.geocaching4locus;

import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.LocusUtils.OnIntentMainFunction;
import locus.api.android.utils.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.receiver.MainActivityBroadcastReceiver;
import com.arcao.geocaching4locus.service.SearchGeocacheService;
import com.arcao.geocaching4locus.task.LocationUpdateTask;
import com.arcao.geocaching4locus.task.LocationUpdateTask.LocationUpdate;
import com.arcao.geocaching4locus.util.Coordinates;
import com.arcao.geocaching4locus.util.LocusTesting;
import com.example.android.cheatsheet.CheatSheet;

public class MainActivity extends FragmentActivity implements LocationUpdate, OnIntentMainFunction {
	private static final String TAG = "G4L|MainActivity";
	
	private static String STATE_LATITUDE = "latitude";
	private static String STATE_LONGITUDE = "longitude";
	private static String STATE_HAS_COORDINATES = "has_coordinates";
	
	private double latitude = Double.NaN;
	private double longitude = Double.NaN;
	private boolean hasCoordinates = false;

	private SharedPreferences prefs;

	private EditText latitudeEditText;
	private EditText longitudeEditText;
	private CheckBox importCachesCheckBox;
	private boolean locusInstalled = false;
	
	private MainActivityBroadcastReceiver mainActivityBroadcastReceiver;
	private LocationUpdateTask locationUpdateTask;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				
		mainActivityBroadcastReceiver = new MainActivityBroadcastReceiver(this);
		hasCoordinates = false;
	
    //showAsPopup();
    setContentView(R.layout.main_activity);
    //prepareActionBar();

    applyMenuItemOnView(R.id.main_activity_option_menu_close, R.id.header_close);
    applyMenuItemOnView(R.id.main_activity_option_menu_preferences, R.id.header_preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		latitude = prefs.getFloat(PrefConstants.LAST_LATITUDE, 0);
		longitude = prefs.getFloat(PrefConstants.LAST_LONGITUDE, 0);
		
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
			LocusUtils.handleIntentMainFunction(getIntent(), this);
		}
		else if (LocusUtils.isIntentSearchList(getIntent())) {
			LocusUtils.handleIntentSearchList(getIntent(), this);
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
	public void onReceived(locus.api.objects.extra.Location locGps, locus.api.objects.extra.Location locMapCenter) {
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
		
		mainActivityBroadcastReceiver.register();
				
		latitudeEditText = (EditText) findViewById(R.id.latitudeEditText);
		longitudeEditText = (EditText) findViewById(R.id.logitudeEditText);
		importCachesCheckBox = (CheckBox) findViewById(R.id.importCachesCheckBox);
		
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
				
		importCachesCheckBox.setChecked(prefs.getBoolean(PrefConstants.IMPORT_CACHES, false));
		importCachesCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor edit = prefs.edit();
				edit.putBoolean(PrefConstants.IMPORT_CACHES, isChecked);
				edit.commit();
			}
		});
		
		if (!locusInstalled && !LocusTesting.isLocusInstalled(this)) {
			locusInstalled = false;
			LocusTesting.showLocusMissingError(this);
			return;
		}
		
		locusInstalled = true;
		
		if (!hasCoordinates) {
			acquireCoordinates();
		} else {
			updateCoordinateTextView();
			requestProgressUpdate();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		if (locationUpdateTask != null)
			locationUpdateTask.detach();
				
		mainActivityBroadcastReceiver.unregister();
		
		Log.i(TAG, "Receiver unregistred.");
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
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
	
	public void onClickClose(View view) {
		finish();
	}
	
	public void onClickSettings(View view) {
		startActivity(new Intent(this, PreferenceActivity.class));
	}
	
	protected void download() {	
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
		
		builder.setMessage(Html.fromHtml(message));
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
		if (SearchGeocacheService.getInstance() != null)
			SearchGeocacheService.getInstance().sendProgressUpdate();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_option_menu, menu);
		return true;
	}

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
	public final boolean onOptionsItemSelected(MenuItem item) {
		if (onOptionsItemSelected(item.getItemId())) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	protected void applyMenuItemOnView(final int resMenuItem, int resView) {
		View v = findViewById(resView);
		if (v == null)
			return;

		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(resMenuItem);
			}
		});
		CheatSheet.setup(v);
	}
}