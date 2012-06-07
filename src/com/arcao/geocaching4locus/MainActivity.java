package com.arcao.geocaching4locus;

import menion.android.locus.addon.publiclib.LocusIntents;
import menion.android.locus.addon.publiclib.LocusIntents.OnIntentMainFunction;
import menion.android.locus.addon.publiclib.geoData.Point;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import com.arcao.geocaching4locus.service.SearchGeocacheService;
import com.arcao.geocaching4locus.util.Coordinates;
import com.arcao.geocaching4locus.util.LocusTesting;

public class MainActivity extends Activity implements LocationListener, OnIntentMainFunction {
	private static final String TAG = "G4L|MainActivity";
	
	private Resources res;
	private LocationManager locationManager;

	private double latitude = Double.NaN;
	private double longitude = Double.NaN;
	private boolean hasCoordinates = false;
	private ProgressDialog pd;

	private Handler handler;
	private SharedPreferences prefs;

	private EditText latitudeEditText;
	private EditText longitudeEditText;
	private CheckBox importCachesCheckBox;
	private boolean locusInstalled = true;
		
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		hasCoordinates = false;

		setContentView(R.layout.main_activity);

		locusInstalled = true;
		if (!LocusTesting.isLocusInstalled(this)) {
			locusInstalled = false;
			LocusTesting.showLocusMissingError(this);
			return;
		}


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
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		IntentFilter filter = new IntentFilter(SearchGeocacheService.ACTION_PROGRESS_UPDATE);
		
		filter.addAction(SearchGeocacheService.ACTION_PROGRESS_UPDATE);
		filter.addAction(SearchGeocacheService.ACTION_PROGRESS_COMPLETE);
		filter.addAction(ErrorActivity.ACTION_ERROR);
		
		registerReceiver(searchGeocacheReceiver, filter);
				
		handler = new Handler();

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
		removeLocationUpdates();
		
		if (pd != null && pd.isShowing())
			pd.dismiss();
		
		unregisterReceiver(searchGeocacheReceiver);
		
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
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		pd = new ProgressDialog(this);
		pd.setMessage(res.getText(R.string.acquiring_gps_location));
		pd.setCancelable(true);
		pd.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				cancelAcquiring();
			}
		});
		pd.setButton(res.getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelAcquiring();
			}
		});
		pd.show();

		// Register the listener with the Location Manager to receive location
		// updates
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			cancelAcquiring();
		}
	}
	
	protected void cancelAcquiring() {
		removeLocationUpdates();

		if (pd != null && pd.isShowing())
			pd.dismiss();	
		
		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location == null)
			location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		if (location == null) {
			latitude = prefs.getFloat(PrefConstants.LAST_LATITUDE, 0F);
			longitude = prefs.getFloat(PrefConstants.LAST_LONGITUDE, 0F);
		} else {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
		
		hasCoordinates = true;
		
		updateCoordinateTextView();
	}

	private void updateCoordinateTextView() {
		if (latitudeEditText != null)
			latitudeEditText.setText(Coordinates.convertDoubleToDeg(latitude, false));
		if (longitudeEditText != null)
			longitudeEditText.setText(Coordinates.convertDoubleToDeg(longitude, true));
	}
	
	protected void removeLocationUpdates() {
		if (locationManager != null)
			locationManager.removeUpdates(this);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		removeLocationUpdates();
		
		if (location == null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					pd.dismiss();
					Log.e(TAG, "onLocationChanged() location is not avaible.");
					showError(R.string.error_location, null);
				}
			});
			return;
		}

		if (!pd.isShowing())
			return;

		latitude = location.getLatitude();
		longitude = location.getLongitude();
		hasCoordinates = true;
		
		updateCoordinateTextView();
				
		Editor editor = prefs.edit();
		editor.putFloat(PrefConstants.LAST_LATITUDE, (float) latitude);
		editor.putFloat(PrefConstants.LAST_LONGITUDE, (float) longitude);
		editor.commit();

		handler.post(new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
			}
		});
	}

	@Override
	public void onProviderDisabled(String provider) {
		removeLocationUpdates();
		
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					pd.setMessage(res.getString(R.string.acquiring_network_location));
				}
			});

			try {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
				return;
			} catch(IllegalArgumentException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		onLocationChanged(locationManager.getLastKnownLocation(provider));
	}

	protected void requestProgressUpdate() {
		if (SearchGeocacheService.getInstance() != null)
			SearchGeocacheService.getInstance().sendProgressUpdate();
	}
	
	private final BroadcastReceiver searchGeocacheReceiver = new BroadcastReceiver() {	
		@Override
		public void onReceive(Context context, final Intent intent) {
			if (SearchGeocacheService.ACTION_PROGRESS_UPDATE.equals(intent.getAction())) {
				if (pd == null || !pd.isShowing()) {
					pd = new ProgressDialog(MainActivity.this);
					pd.setCancelable(true);
					pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							stopService(new Intent(MainActivity.this, SearchGeocacheService.class));
						}
					});
					pd.setButton(res.getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							stopService(new Intent(MainActivity.this, SearchGeocacheService.class));
						}
					});
					pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					pd.setMax(intent.getIntExtra(SearchGeocacheService.PARAM_COUNT, 1));
					pd.setProgress(intent.getIntExtra(SearchGeocacheService.PARAM_CURRENT, 0));
					pd.setMessage(res.getText(R.string.downloading));
					pd.show();
				}
				
				pd.setProgress(intent.getIntExtra(SearchGeocacheService.PARAM_CURRENT, 0));
			} else if (SearchGeocacheService.ACTION_PROGRESS_COMPLETE.equals(intent.getAction())) {
				if (pd != null && pd.isShowing())
					pd.dismiss();
				
				if (intent.getIntExtra(SearchGeocacheService.PARAM_COUNT, 0) != 0 && !MainActivity.this.isFinishing()) {
					MainActivity.this.finish();
				}
			} else if (ErrorActivity.ACTION_ERROR.equals(intent.getAction())) {
				if (pd != null && pd.isShowing())
					pd.dismiss();

				Intent errorIntent = new Intent(MainActivity.this, ErrorActivity.class);
				errorIntent.setAction(ErrorActivity.ACTION_ERROR);
				errorIntent.putExtra(ErrorActivity.PARAM_RESOURCE_ID, intent.getIntExtra(ErrorActivity.PARAM_RESOURCE_ID, 0));
				errorIntent.putExtra(ErrorActivity.PARAM_ADDITIONAL_MESSAGE, intent.getStringExtra(ErrorActivity.PARAM_ADDITIONAL_MESSAGE));
				errorIntent.putExtra(ErrorActivity.PARAM_OPEN_PREFERENCE, intent.getBooleanExtra(ErrorActivity.PARAM_OPEN_PREFERENCE, false));
				errorIntent.putExtra(ErrorActivity.PARAM_EXCEPTION, intent.getSerializableExtra(ErrorActivity.PARAM_EXCEPTION));
				errorIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				
				MainActivity.this.startActivity(errorIntent);
			}
		}		
	};

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}