package com.arcao.geocaching4locus;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.arcao.geocaching4locus.geocaching.CacheType;
import com.arcao.geocaching4locus.geocaching.SimpleGeocache;
import com.arcao.geocaching4locus.util.Account;
import com.arcao.geocaching4locus.util.Coordinates;

public class MainActivity extends Activity implements LocationListener {
	private static final String TAG = "Geocaching4Locus|MainActivity";
	private static final String SERVICE_URL = "http://hg-service.appspot.com/hgservice/search?lat=%f&lon=%f&account=%s&filter=%s&distance=%f&limit=%d";
	//private static final String SERVICE_URL = "http://10.20.20.10:8888/hgservice/search?lat=%f&lon=%f&account=%s&filter=%s&distance=%f&limit=%d";

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		res = getResources();

		if (!isLocusAvailable(this)) {
			Log.e(TAG, "locus not found");
			Toast.makeText(MainActivity.this, res.getString(R.string.locus_not_found), Toast.LENGTH_LONG).show();

			Uri localUri = Uri.parse("market://details?id=menion.android.locus");
			Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
			startActivity(localIntent);
			finish();
			return;
		}

		handler = new Handler();

		setContentView(R.layout.main_activity);

		latitudeEditText = (EditText) findViewById(R.id.latitudeEditText);
		longitudeEditText = (EditText) findViewById(R.id.logitudeEditText);

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
			}
		});

		searchThread = new Thread() {
			@Override
			public void run() {
				try {
					// download caches
					final SimpleGeocache[] caches = downloadCaches(latitude, longitude);

					handler.post(new Runnable() {
						public void run() {
							// call intent
							callLocus(cachesToCategories(caches));
							pd.dismiss();
							MainActivity.this.finish();
						}
					});
				} catch (final Exception e) {
					handler.post(new Runnable() {
						public void run() {
							pd.dismiss();
							Log.e(TAG, "search()", e);
							Toast.makeText(MainActivity.this, res.getString(R.string.error), Toast.LENGTH_LONG).show();
							MainActivity.this.finish();
						}
					});
				}
			}
		};
		searchThread.start();
	}

	protected void acquireCoordinates() {
		// search location
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		pd = ProgressDialog.show(this, null, res.getString(R.string.acquiring_gps_location), false, true, new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
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

	private void callLocus(Map<String, List<SimpleGeocache>> caches) {
		ByteArrayOutputStream baos = null;
		DataOutputStream dos = null;
		try {
			baos = new ByteArrayOutputStream();
			dos = new DataOutputStream(baos);

			// version
			dos.writeInt(2);

			// write objects names
			dos.writeUTF("Geocaches");

			// write category count - here I write three categories. Categories
			// are defined as
			// array of points that share same map icon!
			Set<String> categories = caches.keySet();
			dos.writeInt(categories.size());

			// write categories
			for (String category : categories) {
				writeCategory(dos, category, caches.get(category));
			}

			// flush data to output stream
			dos.flush();

			// create intent with right calling method
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("menion.points:extraDataSomeName"));

			// here put data into intent
			intent.putExtra("extraDataSomeName", baos.toByteArray());

			// finally start activity
			startActivity(intent);
		} catch (Exception e) {
			Log.e(TAG, "callLocus()", e);
			throw new IllegalArgumentException(e);
		} finally {
			try {
				if (baos != null) {
					baos.close();
					baos = null;
				}
				if (dos != null) {
					dos.close();
					dos = null;
				}
			} catch (Exception e) {
				Log.e(TAG, "callLocus()", e);
			}
		}
	}

	private void writeCategory(DataOutputStream dos, String category, List<SimpleGeocache> caches) {
		try {
			// convert resource to byte array
			Bitmap bitmap = BitmapFactory.decodeResource(res, getBitmapForCache(caches.get(0)));
			ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos2);
			byte[] image = baos2.toByteArray();
			baos2.close();

			// write image data or size '0' and no data if use default image of
			// Locus (currently just red dot) - so if you want write image use
			// this
			dos.writeInt(image.length); // image size
			dos.write(image); // image data - and if you don't want use only
								// this dos.writeInt(0);

			// write all points now
			dos.writeInt(caches.size());
			for (SimpleGeocache cache : caches) {
				// write item name
				dos.writeUTF(cache.getGeoCode() + ": " + cache.getName());
				// write item description
				dos.writeUTF(getDescription(cache));
				// extra values (describe below - optional)
				dos.writeUTF("");
				// write latitude
				dos.writeDouble(cache.getLatitude());
				// write longitude
				dos.writeDouble(cache.getLongitude());
			}
		} catch (Exception e) {
			Log.e(TAG, "writeCategory()", e);
		}
	}

	protected int getBitmapForCache(SimpleGeocache cache) {
		switch (cache.getCacheType()) {
			case EarthCache:
				return R.drawable.marker_cache_earth;
			case EventCache:
				return R.drawable.marker_cache_event;
			case GpsAdventuresExhibit:
				return R.drawable.marker_cache_mystery;
			case LetterboxHybrid:
				return R.drawable.marker_cache_letterbox;
			case LocationlessCache:
				return R.drawable.marker_cache_locationless;
			case MultiCache:
				return R.drawable.marker_cache_multi;
			case ProjectApeCache:
				return R.drawable.marker_cache_ape;
			case TraditionalCache:
				return R.drawable.marker_cache_traditional;
			case UnknownCache:
				return R.drawable.marker_cache_mystery;
			case VirtualCache:
				return R.drawable.marker_cache_virtual;
			case WebcamCache:
				return R.drawable.marker_cache_traditional;
			case WherigoCache:
				return R.drawable.marker_cache_wherigo;
			default:
				return R.drawable.marker_cache_traditional;
		}
	}

	protected String getDescription(SimpleGeocache cache) {
		return res.getString(
				R.string.description,
				cache.getGeoCode(),
				cache.getAuthorName(),
				cache.getCacheType().toString(),
				cache.getContainerType().toString(),
				cache.getDifficultyRating(),
				cache.getTerrainRating()
				);
	}

	protected Map<String, List<SimpleGeocache>> cachesToCategories(SimpleGeocache[] caches) {
		Map<String, List<SimpleGeocache>> result = new HashMap<String, List<SimpleGeocache>>();

		for (SimpleGeocache cache : caches) {
			String key = cache.getCacheType() + "|" + cache.isFound() + "|" + !cache.isAvailable();
			if (!result.containsKey(key)) {
				result.put(key, new ArrayList<SimpleGeocache>());
			}

			result.get(key).add(cache);
		}
		return result;
	}

	protected String getFilterUrlParam() {
		Vector<String> filter = new Vector<String>();

		for (int i = 0; i < 12; i++) {
			if (prefs.getBoolean("filter_" + i, true)) {
				filter.add(CacheType.values()[i].toString());
			}
		}

		return URLEncoder.encode(TextUtils.join(",", filter));
	}

	protected SimpleGeocache[] downloadCaches(double latitude, double longitude) throws IOException {
		boolean skipFound = prefs.getBoolean("filter_skip_found", false);

		double distance = prefs.getFloat("distance", 160.9344F);
		if (!prefs.getBoolean("imperial_units", false)) {
			distance = distance * 1.609344;
		}

		int limit = prefs.getInt("filter_count_of_caches", 50);
		
		String accountData = Account.encrypt("", "", "");
		if (skipFound)
			accountData = account.encrypt();

		URL url = new URL(String.format((Locale) null, SERVICE_URL, latitude, longitude, accountData, getFilterUrlParam(), distance, limit));
		Log.i(TAG, "downloading " + url);
		//Log.i(TAG, account.toString());

		HttpURLConnection uc = (HttpURLConnection) url.openConnection();
		if (prefs.getBoolean("compression", false)) {
			uc.setRequestProperty("Accept", "text/plain, multipart/x-datastream, gzip, */*; q=0.01");
			uc.setRequestProperty("Accept-Encoding", "gzip");
		}

		final String encoding = uc.getContentEncoding();
		InputStream is;

		if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
			Log.i(TAG, "downloadCaches(): GZIP OK");
			is = new GZIPInputStream(uc.getInputStream());
		} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
			Log.i(TAG, "downloadCaches(): DEFLATE OK");
			is = new InflaterInputStream(uc.getInputStream(), new Inflater(true));
		} else {
			Log.i(TAG, "downloadCaches(): WITHOUT ENCODING");
			is = uc.getInputStream();
		}

		Log.i(TAG, "parsing caches...");
		DataInputStream dis = new DataInputStream(is);

		// protocol version
		if (dis.readInt() != 1)
			throw new IOException("Wrong protocol version.");

		// result: 0 = OK
		if (dis.readInt() != 0)
			throw new IOException("Response error code is not 0.");

		// get account data
		accountData = dis.readUTF();
		if (skipFound) {
			account = Account.decrypt(accountData);
			if (account.getSession() != null && account.getSession().length() > 0) {
				Editor edit = prefs.edit();
				edit.putString("session", account.getSession());
				edit.commit();
			}
		}

		// num of caches
		int cacheCount = dis.readInt();
		Log.i(TAG, "found caches: " + cacheCount);
		
		SimpleGeocache[] caches = new SimpleGeocache[cacheCount];
		for (int i = 0; i < cacheCount; i++) {
			caches[i] = SimpleGeocache.load(dis);
			//Log.i(TAG, caches[i].toString());
		}

		Log.i(TAG, "caches parsed!");
		return caches;
	}

	public static boolean isLocusAvailable(Activity activity) {
		try {
			// set intent
			final PackageManager packageManager = activity.getPackageManager();
			final Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("menion.points:x"));

			// return true or false
			return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
		} catch (Exception e) {
			return false;
		}
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

		// if (searchThread != null && !searchThread.isAlive())
		// searchThread.start();
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