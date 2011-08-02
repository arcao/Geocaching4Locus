package com.arcao.geocaching4locus;

import geocaching.api.AbstractGeocachingApi;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.type.CacheType;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.exception.InvalidCredentialsException;
import geocaching.api.exception.InvalidSessionException;
import geocaching.api.impl.IPhoneGeocachingApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import menion.android.locus.addon.publiclib.DisplayData;
import menion.android.locus.addon.publiclib.LocusUtils;
import menion.android.locus.addon.publiclib.geoData.PointsData;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
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

import com.arcao.geocaching4locus.provider.DataStorageProvider;
import com.arcao.geocaching4locus.util.Account;
import com.arcao.geocaching4locus.util.Coordinates;

public class MainActivity extends Activity implements LocationListener {
	private static final String TAG = "Geocaching4Locus|MainActivity";
	
	private static final int LOCUS_MIN_VERSION_CODE = 99;
	private static final String LOCUS_MIN_VERSION_NAME = "1.9.5.1";

	private Resources res;
	private Thread searchThread;
	private LocationManager locationManager;

	private double latitude;
	private double longitude;
	private boolean hasCoordinates = false;
	private ProgressDialog pd;

	private Handler handler;
	private SharedPreferences prefs;
	private Account account = null;

	private EditText latitudeEditText;
	private EditText longitudeEditText;

	private CheckBox simpleCacheDataCheckBox;

	private CheckBox importCachesCheckBox;
	
	private boolean cancelDownload = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		Log.i(TAG, "Locus version: " + LocusUtils.getLocusVersion(this));
		
		int locusVersionCode = LocusUtils.getLocusVersionCode(this);
		
		if (locusVersionCode == -1) {
			Log.e(TAG, "locus not found");
			showError(R.string.error_locus_not_found, LOCUS_MIN_VERSION_NAME, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Uri localUri = Uri.parse("https://market.android.com/details?id=" + LocusUtils.getLocusDefaultPackageName(MainActivity.this));
					Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
					startActivity(localIntent);
					finish();
				}
			});
			return;
		}
		
		if (locusVersionCode < LOCUS_MIN_VERSION_CODE) {
			showError(R.string.error_locus_old, LOCUS_MIN_VERSION_NAME, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Uri localUri = Uri.parse("https://market.android.com/details?id=" + LocusUtils.getLocusDefaultPackageName(MainActivity.this));
					Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
					startActivity(localIntent);
					finish();
				}
			});
			return;
		}

		handler = new Handler();

		setContentView(R.layout.main_activity);

		latitudeEditText = (EditText) findViewById(R.id.latitudeEditText);
		longitudeEditText = (EditText) findViewById(R.id.logitudeEditText);
		simpleCacheDataCheckBox = (CheckBox) findViewById(R.id.simpleCacheDataCheckBox);
		importCachesCheckBox = (CheckBox) findViewById(R.id.importCachesCheckBox);

		if (getIntent().getAction().equals("menion.android.locus.ON_POINT_ACTION")) {
			latitude = getIntent().getDoubleExtra("latitude", 0.0);
			longitude = getIntent().getDoubleExtra("longitude", 0.0);
			double alt = getIntent().getDoubleExtra("altitude", 0.0);
			double acc = getIntent().getDoubleExtra("accuracy", 0.0);
			Log.i(TAG, "Called from Locus: lat=" + latitude + "; lon=" + longitude + "; alt=" + alt + "; acc=" + acc);

			latitudeEditText.setText(Coordinates.convertDoubleToDeg(latitude, false));
			longitudeEditText.setText(Coordinates.convertDoubleToDeg(longitude, true));

			hasCoordinates = true;
		}

		latitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					double deg = Coordinates.convertDegToDouble(latitudeEditText.getText().toString());
					if (Double.isNaN(deg)) {
						latitudeEditText.setText("N/A");
					} else {
						latitudeEditText.setText(Coordinates.convertDoubleToDeg(deg, false));
					}
				}
			}
		});

		longitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					double deg = Coordinates.convertDegToDouble(longitudeEditText.getText().toString());
					if (Double.isNaN(deg)) {
						longitudeEditText.setText("N/A");
					} else {
						longitudeEditText.setText(Coordinates.convertDoubleToDeg(deg, true));
					}
				}
			}
		});
		
		simpleCacheDataCheckBox.setChecked(prefs.getBoolean("simple_cache_data", false));
		simpleCacheDataCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor edit = prefs.edit();
				edit.putBoolean("simple_cache_data", isChecked);
				edit.commit();
			}
		});
		
		importCachesCheckBox.setChecked(prefs.getBoolean("import_caches", false));
		importCachesCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor edit = prefs.edit();
				edit.putBoolean("import_caches", isChecked);
				edit.commit();
			}
		});

		if (!hasCoordinates) {
			acquireCoordinates();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		String session = prefs.getString("session", null);

		account = new Account(userName, password, session);
	}
	
	@Override
	protected void onStop() {
		if (locationManager != null)
			locationManager.removeUpdates(this);
		super.onStop();
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
		cancelDownload = false;
		
		latitude = Coordinates.convertDegToDouble(latitudeEditText.getText().toString());
		longitude = Coordinates.convertDegToDouble(longitudeEditText.getText().toString());

		if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(MainActivity.this, R.string.wrong_coordinates, Toast.LENGTH_LONG);
				}
			});
		}

		pd = ProgressDialog.show(this, null, res.getString(R.string.downloading), false, true, new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				cancelDownload = true;
			}
		});

		searchThread = new Thread() {
			@Override
			public void run() {
				try {
					// download caches
					final List<SimpleGeocache> caches = downloadCaches(latitude, longitude);
					if (cancelDownload)
						return;

					handler.post(new Runnable() {
						public void run() {
							// call intent
							callLocus(caches);
							pd.dismiss();
							MainActivity.this.finish();
						}
					});
				} catch (final Exception e) {
					handler.post(new Runnable() {
						public void run() {
							pd.dismiss();
							Log.e(TAG, "search()", e);
							if (e instanceof InvalidCredentialsException) {
								showError(R.string.error_credentials, null);
							} else {
								String message = e.getMessage();
								if (message == null)
									message = "";
								showError(R.string.error, String.format("<br>%s<br> <br>Exception: %s<br>File: %s<br>Line: %d", message, e.getClass().getSimpleName(), e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber()));
							}
						}
					});
				}
			}
		};
		searchThread.start();
	}
	
	protected void showError(int errorResId, String additionalMessage) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = String.format(res.getString(errorResId), additionalMessage);
		
		builder.setMessage(Html.fromHtml(message));
		builder.setTitle(R.string.error_title);
		builder.setPositiveButton(R.string.ok_button, null);
		builder.show();
	}
	
	protected void showError(int errorResId, String additionalMessage, DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = String.format(res.getString(errorResId), additionalMessage);
		
		builder.setMessage(Html.fromHtml(message));
		builder.setTitle(R.string.error_title);
		builder.setPositiveButton(R.string.ok_button, onClickListener);
		builder.show();
	}

	protected void acquireCoordinates() {
		// search location
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		pd = ProgressDialog.show(this, null, res.getString(R.string.acquiring_gps_location), false, true, new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				locationManager.removeUpdates(MainActivity.this);
				
				Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location == null)
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				
				if (location == null) {
					latitude = prefs.getFloat("latitude", 0F);
					longitude = prefs.getFloat("longitude", 0F);
				} else {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				}
				
				latitudeEditText.setText(Coordinates.convertDoubleToDeg(latitude, false));
				longitudeEditText.setText(Coordinates.convertDoubleToDeg(longitude, true));
			}
		});


		// Register the listener with the Location Manager to receive location
		// updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	private void callLocus(List<SimpleGeocache> caches) {
		boolean importCaches = prefs.getBoolean("import_caches", false);
		
		try {
			ArrayList<PointsData> pointDataCollection = new ArrayList<PointsData>();
			
			// beware there is row limit in DataStorageProvider (1MB per row - serialized PointsData is one row)
			// so we split points into several PointsData object with same uniqueName - Locus merge it automatically
			// since version 1.9.5
			PointsData points = new PointsData("Geocaching");
			for (SimpleGeocache cache : caches) {
				if (points.getPoints().size() >= 10) {
					pointDataCollection.add(points);
					points = new PointsData("Geocaching");
				}
				// convert SimpleGeocache to Point
				points.addPoint(cache.toPoint());
			}
			
			if (!points.getPoints().isEmpty())
				pointDataCollection.add(points);
			
			DisplayData.sendDataCursor(this, pointDataCollection, DataStorageProvider.URI, importCaches);
		} catch (Exception e) {
			Log.e(TAG, "callLocus()", e);
			throw new IllegalArgumentException(e);
		}
	}
	
	private CacheType[] getCacheTypeFilterResult() {
		Vector<CacheType> filter = new Vector<CacheType>();

		for (int i = 0; i < CacheType.values().length; i++) {
			if (prefs.getBoolean("filter_" + i, true)) {
				filter.add(CacheType.values()[i]);
			}
		}

		return filter.toArray(new CacheType[0]);
	}

	protected List<SimpleGeocache> downloadCaches(double latitude, double longitude) throws GeocachingApiException {
		boolean skipFound = prefs.getBoolean("filter_skip_found", false);
		
		boolean simpleCacheData = prefs.getBoolean("simple_cache_data", false);
		
		double distance = prefs.getFloat("distance", 160.9344F);
		if (!prefs.getBoolean("imperial_units", false)) {
			distance = distance * 1.609344;
		}

		int limit = prefs.getInt("filter_count_of_caches", 50);
		
		AbstractGeocachingApi api = new IPhoneGeocachingApi();
		try {
			if (skipFound) {
				if (account.getUserName() == null || account.getUserName().length() == 0 || account.getPassword() == null || account.getPassword().length() == 0)
					throw new InvalidCredentialsException("Username or password is empty.");
				
				if (account.getSession() == null || account.getSession().length() == 0) {
					api.openSession(account.getUserName(), account.getPassword());
				} else {
					api.openSession(account.getSession());
				}
			} else {
				api.openSession(null);
			}
		} catch (InvalidCredentialsException e) {
			Log.e(TAG, "Creditials not valid.", e);
			throw e;
		}
		
		if (cancelDownload)
			return null;
		
		try {
			List<SimpleGeocache> caches = api.getCachesByCoordinates(latitude, longitude, 0, limit - 1, (float) distance, getCacheTypeFilterResult());
			int count = caches.size();
			
			if (cancelDownload)
				return caches;
					
			Log.i(TAG, "found caches: " + count);
			if (!simpleCacheData) {
				prepareDownloadStatus(count);

				for(int i = 0; i < count; i++) {
					SimpleGeocache cache = caches.get(i);
					updateDownloadStatus(i + 1, count, cache.getGeoCode(), cache.getName());
					caches.set(i, api.getCache(cache.getGeoCode()));
					
					if (cancelDownload)
						return caches;
				}
			}
			
			return caches;
		} catch (InvalidSessionException e) {
			try {
				api.openSession(account.getUserName(), account.getPassword());
			} catch (InvalidCredentialsException ex) {
				Log.e(TAG, "Creditials not valid.", ex);
				throw e;
			}
						
			try {
				List<SimpleGeocache> caches = api.getCachesByCoordinates(latitude, longitude, 0, limit - 1, (float) distance, getCacheTypeFilterResult());
				int count = caches.size();

				if (cancelDownload)
					return caches;
						
				Log.i(TAG, "found caches: " + count);
				if (!simpleCacheData) {
					prepareDownloadStatus(count);
					for(int i = 0; i < count; i++) {
						SimpleGeocache cache = caches.get(i);
						updateDownloadStatus(i + 1, count, cache.getGeoCode(), cache.getName());
						caches.set(i, api.getCache(cache.getGeoCode()));

						if (cancelDownload)
							return caches;
					}
				}
				return caches;
			} catch (InvalidSessionException ex) {
				Log.e(TAG, "Creditials not valid.", ex);
				throw ex;
			}
		} finally {
			if (skipFound) {
				account.setSession(api.getSession());
				if (account.getSession() != null && account.getSession().length() > 0) {
					Editor edit = prefs.edit();
					edit.putString("session", account.getSession());
					edit.commit();
				}
			}
		}
	}
	
	private void prepareDownloadStatus(final int count) {
		handler.post(new Runnable() {
			public void run() {
				if (pd != null && pd.isShowing())
					pd.dismiss();
				
				pd = new ProgressDialog(MainActivity.this);
				pd.setCancelable(true);
				pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						cancelDownload = true;
					}
				});
				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				pd.setProgress(0);
				pd.setMax(count);
				pd.setMessage("");
				pd.show();
			}
		});
	}
	
	private void updateDownloadStatus(final int current, final int count, final String geoCode, final String name) {
		Log.i(TAG, String.format("downloading (%d/%d): %s - %s", current, count, geoCode, name));
		handler.post(new Runnable() {
			public void run() {
				pd.setMessage(res.getString(R.string.downloading_cache, geoCode, name, current, count));
				pd.setProgress(current);
			}
		});
	}
		
	public void onLocationChanged(Location location) {
		locationManager.removeUpdates(this);
		if (location == null) {
			handler.post(new Runnable() {
				public void run() {
					pd.dismiss();
					Log.e(TAG, "onLocationChanged() location is not avaible.");
					Toast.makeText(MainActivity.this, res.getString(R.string.error_location), Toast.LENGTH_LONG).show();
					MainActivity.this.finish();
				}
			});
			return;
		}

		if (!pd.isShowing())
			return;

		latitude = location.getLatitude();
		longitude = location.getLongitude();

		latitudeEditText.setText(Coordinates.convertDoubleToDeg(latitude, false));
		longitudeEditText.setText(Coordinates.convertDoubleToDeg(longitude, true));
		
		Editor editor = prefs.edit();
		editor.putFloat("latitude", (float) latitude);
		editor.putFloat("longitude", (float) longitude);
		editor.commit();

		handler.post(new Runnable() {
			public void run() {
				pd.dismiss();
			}
		});
	}

	public void onProviderDisabled(String provider) {
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
			locationManager.removeUpdates(this);

			handler.post(new Runnable() {
				public void run() {
					pd.setMessage(res.getString(R.string.acquiring_network_location));
				}
			});

			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			onLocationChanged(locationManager.getLastKnownLocation(provider));
		}
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}