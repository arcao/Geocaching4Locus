package com.arcao.geocaching4locus;

import geocaching.api.AbstractGeocachingApiV2;
import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.type.CacheType;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.exception.InvalidCredentialsException;
import geocaching.api.exception.InvalidSessionException;
import geocaching.api.impl.LiveGeocachingApi;
import geocaching.api.impl.live_geocaching_api.filter.CacheFilter;
import geocaching.api.impl.live_geocaching_api.filter.GeocacheExclusionsFilter;
import geocaching.api.impl.live_geocaching_api.filter.GeocacheTypeFilter;
import geocaching.api.impl.live_geocaching_api.filter.NotFoundByUsersFilter;
import geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import menion.android.locus.addon.publiclib.DisplayData;
import menion.android.locus.addon.publiclib.LocusUtils;
import menion.android.locus.addon.publiclib.geoData.PointsData;

import org.osgi.framework.Version;

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

import com.arcao.geocaching4locus.provider.DataStorageProvider;
import com.arcao.geocaching4locus.util.Account;
import com.arcao.geocaching4locus.util.Coordinates;
import com.arcao.geocaching4locus.util.UserTask;
import com.arcao.geocaching4locus.util.UserTask.Status;

public class MainActivity extends Activity implements LocationListener {
	private static final String TAG = "Geocaching4Locus|MainActivity";
	
	private static final Version LOCUS_MIN_VERSION = Version.parseVersion("1.9.5.2");

	private Resources res;
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
	
	private SearchTask searchTask;
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
			
		Version locusVersion = Version.parseVersion(LocusUtils.getLocusVersion(this));
		Log.i(TAG, "Locus version: " + locusVersion);
		
		if (locusVersion.compareTo(LOCUS_MIN_VERSION) < 0) {
			showError(locusVersion == Version.emptyVersion ? R.string.error_locus_not_found : R.string.error_locus_old, LOCUS_MIN_VERSION.toString(), new DialogInterface.OnClickListener() {
				@Override
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
			Log.i(TAG, "Called from Locus: lat=" + latitude + "; lon=" + longitude);

			latitudeEditText.setText(Coordinates.convertDoubleToDeg(latitude, false));
			longitudeEditText.setText(Coordinates.convertDoubleToDeg(longitude, true));

			hasCoordinates = true;
		}

		latitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
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
			@Override
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
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Editor edit = prefs.edit();
				edit.putBoolean("simple_cache_data", isChecked);
				edit.commit();
			}
		});
		
		importCachesCheckBox.setChecked(prefs.getBoolean("import_caches", false));
		importCachesCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
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
	
	@Override
	protected void onDestroy() {
		if (searchTask != null && searchTask.getStatus() == Status.RUNNING)
			searchTask.cancel(true);

		super.onDestroy();
	}

	protected void download() {	
		latitude = Coordinates.convertDegToDouble(latitudeEditText.getText().toString());
		longitude = Coordinates.convertDegToDouble(longitudeEditText.getText().toString());

		if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
			showError(R.string.wrong_coordinates, null);
		}
		
		searchTask = new SearchTask(latitude, longitude);
		searchTask.execute();
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
	
	protected void showError(int errorResId, String additionalMessage, DialogInterface.OnClickListener onClickListener) {
		if (isFinishing())
			return;
		
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
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	
	protected void cancelAcquiring() {
		if (pd != null && pd.isShowing())
			pd.dismiss();
		
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


	private void callLocus(List<SimpleGeocache> caches) {
		boolean importCaches = prefs.getBoolean("import_caches", false);
		
		try {
			ArrayList<PointsData> pointDataCollection = new ArrayList<PointsData>();
			
			// beware there is row limit in DataStorageProvider (1MB per row - serialized PointsData is one row)
			// so we split points into several PointsData object with same uniqueName - Locus merge it automatically
			// since version 1.9.5
			PointsData points = new PointsData("Geocaching");
			for (SimpleGeocache cache : caches) {
				if (cache instanceof Geocache) {
					Geocache geocache = (Geocache) cache;
					Log.i(TAG, geocache.getGeoCode() + ": " + geocache.getLongDescription());
				}
				
				if (points.getPoints().size() >= 50) {
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
	
	@Override
	public void onLocationChanged(Location location) {
		locationManager.removeUpdates(this);
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

		latitudeEditText.setText(Coordinates.convertDoubleToDeg(latitude, false));
		longitudeEditText.setText(Coordinates.convertDoubleToDeg(longitude, true));
		
		Editor editor = prefs.edit();
		editor.putFloat("latitude", (float) latitude);
		editor.putFloat("longitude", (float) longitude);
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
		if (LocationManager.GPS_PROVIDER.equals(provider)) {
			locationManager.removeUpdates(this);

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

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	private class SearchTask extends UserTask<Void, Integer, List<SimpleGeocache>> {
		private final ProgressDialog pd = new ProgressDialog(MainActivity.this);
		private final static int MAX_PER_PAGE = 10;
		
		private final boolean skipFound;
		private final boolean simpleCacheData;
		private double distance;
		private final int limit;
		
		private final double latitude;
		private final double longitude;
		
		public SearchTask(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
			
			skipFound = prefs.getBoolean("filter_skip_found", false);
			simpleCacheData = prefs.getBoolean("simple_cache_data", false);
			
			distance = prefs.getFloat("distance", 160.9344F);
			if (!prefs.getBoolean("imperial_units", false)) {
				distance = distance * 1.609344;
			}

			limit = prefs.getInt("filter_count_of_caches", 50);
		}
		
		@Override
		protected void onPreExecute() {
			pd.setCancelable(true);
			pd.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					cancel(true);
				}
			});
			pd.setButton(res.getText(R.string.cancel_button), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancel(true);
				}
			});
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setProgress(0);
			pd.setMax(limit);
			pd.setMessage(res.getText(R.string.downloading));
			pd.show();
		}

		@Override
		protected List<SimpleGeocache> doInBackground(Void... params) throws Exception {
			return downloadCaches(latitude, longitude);
		}
		
		@Override
		protected void onPostExecute(List<SimpleGeocache> result) {
			callLocus(result);
			MainActivity.this.finish();
		}
				
		@Override
		protected void onException(Throwable e) {
			if (pd.isShowing())
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
		
		@Override
		protected void onFinally() {
			if (pd.isShowing())
				pd.dismiss();
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			if (pd.isShowing())
				pd.setProgress(values[0]);
		}
		
		protected List<SimpleGeocache> downloadCaches(double latitude, double longitude) throws GeocachingApiException {
			final List<SimpleGeocache> caches = new ArrayList<SimpleGeocache>();
						
			if (account.getUserName() == null || account.getUserName().length() == 0 || account.getPassword() == null || account.getPassword().length() == 0)
				throw new InvalidCredentialsException("Username or password is empty.");
					
			AbstractGeocachingApiV2 api = new LiveGeocachingApi();
			login(api, account);
			
			if (isCancelled())
				return null;
			
			try {
				int start = 0;
				while (start < limit) {
					int perPage = (limit - start < MAX_PER_PAGE) ? limit - start : MAX_PER_PAGE;
					
					List<SimpleGeocache> cachesToAdd = api.searchForGeocachesJSON(simpleCacheData, start, perPage, -1, -1, new CacheFilter[] {
							new PointRadiusFilter(latitude, longitude, (long) (distance * 1000)),
							new GeocacheTypeFilter(getCacheTypeFilterResult()),
							new GeocacheExclusionsFilter(false, true, null),
							new NotFoundByUsersFilter(skipFound ? account.getUserName() : null)
					});
					
					if (cachesToAdd.size() == 0)
						break;
					
					if (isCancelled())
						return null;
					
					caches.addAll(cachesToAdd);
					
					start = start + perPage;
					
					publishProgress(start);
				}
				int count = caches.size();
				
				Log.i(TAG, "found caches: " + count);

				return caches;
			} catch (InvalidSessionException e) {
				account.setSession(null);
				
				Editor edit = prefs.edit();
				edit.remove("session");
				edit.commit();
				
				return downloadCaches(latitude, longitude);
			} finally {
				account.setSession(api.getSession());
				if (account.getSession() != null && account.getSession().length() > 0) {
					Editor edit = prefs.edit();
					edit.putString("session", account.getSession());
					edit.commit();
				}
			}
		}

		private void login(AbstractGeocachingApiV2 api, Account account) throws GeocachingApiException, InvalidCredentialsException {
			try {
				if (account.getSession() == null || account.getSession().length() == 0) {
					api.openSession(account.getUserName(), account.getPassword());
				} else {
					api.openSession(account.getSession());
				}
			} catch (InvalidCredentialsException e) {
				Log.e(TAG, "Creditials not valid.", e);
				throw e;
			}
		}
	}
}