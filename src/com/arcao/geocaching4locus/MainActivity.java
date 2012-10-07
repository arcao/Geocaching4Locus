package com.arcao.geocaching4locus;

import menion.android.locus.addon.publiclib.LocusIntents;
import menion.android.locus.addon.publiclib.LocusIntents.OnIntentMainFunction;
import menion.android.locus.addon.publiclib.geoData.Point;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
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

public class MainActivity extends FragmentActivity implements LocationUpdate, OnIntentMainFunction {
	private static final String TAG = "G4L|MainActivity";
	
	public static int DOWNLOAD_PROGRESS_DIALOG_ID = 1;
	
	private Resources res;

	private double latitude = Double.NaN;
	private double longitude = Double.NaN;
	private boolean hasCoordinates = false;

	private SharedPreferences prefs;

	private EditText latitudeEditText;
	private EditText longitudeEditText;
	private CheckBox importCachesCheckBox;
	private boolean locusInstalled = true;
	
	private MainActivityBroadcastReceiver mainActivityBroadcastReceiver;
	private LocationUpdateTask locationUpdateTask;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		mainActivityBroadcastReceiver = new MainActivityBroadcastReceiver(this);
		hasCoordinates = false;

		setContentView(R.layout.main_activity);

		locusInstalled = true;
		if (!LocusTesting.isLocusInstalled(this)) {
			locusInstalled = false;
			LocusTesting.showLocusMissingError(this);
			return;
		}

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		latitude = prefs.getFloat(PrefConstants.LAST_LATITUDE, 0);
		longitude = prefs.getFloat(PrefConstants.LAST_LONGITUDE, 0);
		
		
		if (LocusIntents.isIntentOnPointAction(getIntent())) {
			Point p = LocusIntents.handleIntentOnPointAction(getIntent());
			if (p == null) {
				Toast.makeText(this, "Wrong INTENT - no point!", Toast.LENGTH_SHORT).show();
			} else {
				latitude = p.getLocation().getLatitude();
				longitude = p.getLocation().getLongitude();
				Log.i(TAG, "Called from Locus: lat=" + latitude + "; lon=" + longitude);

				hasCoordinates = true;
			}
		}
		if (LocusIntents.isIntentMainFunction(getIntent())) {
			LocusIntents.handleIntentMainFunction(getIntent(), this);
		}
		
		if (SearchGeocacheService.getInstance() != null && !SearchGeocacheService.getInstance().isCanceled()) {
			hasCoordinates = true;
		}
	}
	
	@Override
	public void onLocationReceived(boolean gpsEnabled, Location locGps, Location locMapCenter) {
		Location l = locMapCenter;
		latitude = l.getLatitude();
		longitude = l.getLongitude();

		Log.i(TAG, "Called from Locus: lat=" + latitude + "; lon=" + longitude);
		
		hasCoordinates = true;
	}
	
	@Override
	public void onFailed() {}
	
	@Override
	protected void onResume() {	
		super.onResume();
		
		mainActivityBroadcastReceiver.register();
				
		// temporary fix for bug
		if (findViewById(R.id.latitudeEditText) == null)
			setContentView(R.layout.main_activity);
		
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
		
		if (!hasCoordinates) {
			if (locusInstalled)
				acquireCoordinates();
		} else {
			updateCoordinateTextView();
			requestProgressUpdate();
		}
		
		Log.i(TAG, "Receiver registred.");
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.main_activity_option_menu_preferences:
				startActivity(new Intent(this, PreferenceActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
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
		String message = String.format(res.getString(errorResId), additionalMessage);
		
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
}