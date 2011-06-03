package com.arcao.geocaching4locus;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.arcao.geocaching4locus.geocaching.CacheType;
import com.arcao.geocaching4locus.geocaching.SimpleGeocache;
import com.arcao.geocaching4locus.util.Account;

public class MainActivity extends Activity implements LocationListener {
	private static final String TAG = "Geocaching4Locus|MainActivity";
	private static final String SERVICE_URL = "http://hg-service.appspot.com/hgservice/search?lat=%f&lon=%f&account=%s&filter=%s";
	//private static final String SERVICE_URL = "http://10.20.20.10:8888/hgservice/search_locus?lat=%f&lon=%f";
	
	private Resources res;
	private Thread searchThread;
	private LocationManager locationManager;
	
	private double latitude;
	private double longitude;
	private boolean hasCoordinates = false;
	private ProgressDialog pd;
	
	private static Handler handler;
	private static SharedPreferences prefs;
	private static Account account = null;
	
	private Button coordinateSourceButton;
	private Button filterButton;

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
		
		coordinateSourceButton = (Button) findViewById(R.id.coordinates);
		filterButton = (Button) findViewById(R.id.filter);
		
		if (getIntent().getAction().equals("menion.android.locus.ON_POINT_ACTION")) {
			latitude = getIntent().getDoubleExtra("latitude", 0.0);
			longitude = getIntent().getDoubleExtra("longitude", 0.0);
			double alt = getIntent().getDoubleExtra("altitude", 0.0);
			double acc = getIntent().getDoubleExtra("accuracy", 0.0);
			Log.i(TAG, "Called from Locus: lat=" + latitude + "; lon=" + longitude + "; alt=" + alt + "; acc=" + acc);
			
			// fill button text
			coordinateSourceButton.setText(formatCoordinates(latitude, longitude));
			
			hasCoordinates = true;
		}
	}
	
	private String formatCoordinates(double latitude, double longitude) {
		char latFlag = (latitude > 0) ? 'N' : 'S';
		char lonFlag = (longitude > 0) ? 'E' : 'W';
		
		int latDeg = (int) Math.abs(latitude);
		int lonDeg = (int) Math.abs(longitude);
		
		float latMin = (float) ((latitude - latDeg) * 60F);
		float lonMin = (float) ((longitude - lonDeg) * 60F);
		
		return String.format("%c %d\u00b0 %#.3f\u2032 | %c %d\u00b0 %#.3f\u2032", latFlag, latDeg, latMin, lonFlag, lonDeg, lonMin);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		String session = prefs.getString("session", null);
		
		if (account == null) {
			account = new Account(userName, password, session);
		}
		
		if (!userName.equals(account.getUserName()) || !password.equals(account.getPassword())) {
			account = new Account(userName, password);
		}
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
	
	public void onClickCoordinates(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.coordinate_sources_title);
		builder.setItems(R.array.coordinate_sources_value, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        switch (item) {
		        case 0:
		        	coordinateSourceButton.setText(getResources().getStringArray(R.array.coordinate_sources_value)[item]);
		        	break;
		        case 1:
		        	// show coordinate dialog
		        	break;
		        }
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	public void onClickFilter(View view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.filter_title);
		
		String[] filterValues = new String[CacheType.values().length];
		for (int i=0; i < filterValues.length; i++) {
			filterValues[i] = CacheType.values()[i].toString();
		}
		
		final boolean[] checkedState = new boolean[filterValues.length];
		
		builder.setMultiChoiceItems(filterValues, checkedState, new DialogInterface.OnMultiChoiceClickListener() {
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checkedState[which] = isChecked;				
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
		Log.i(TAG, "Filter: " + checkedState);
	}
	
	public void onClickSearch(View view) {
		search();
	}
	

	protected void search() {
		pd = ProgressDialog.show(this, null, res.getString(R.string.acquiring_gps_location), false, true, new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				MainActivity.this.finish();
			}
		});
		
		searchThread = new Thread() {
			@Override
			public void run() {
				try {
					handler.post(new Runnable() {
						public void run() {
							pd.setMessage(res.getString(R.string.downloading));
						}
					});
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
		
		if (hasCoordinates) {
			searchThread.start();
			return;
		}
		
		// search location
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}

	private void callLocus(Map<CacheType, List<SimpleGeocache>> caches) {
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
			Set<CacheType> categories = caches.keySet();
			dos.writeInt(categories.size());

			// write categories
			for (CacheType category : categories) {
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

	private void writeCategory(DataOutputStream dos, CacheType category, List<SimpleGeocache> caches) {
		try {
			// convert resource to byte array
			Bitmap bitmap = BitmapFactory.decodeResource(res, getBitmapForCategory(category));
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
	
	protected int getBitmapForCategory(CacheType category) {
		switch (category) {
		case EarthCache:
			return R.drawable.type_earth;
		case EventCache:
			return R.drawable.type_event;
		case GpsAdventuresExhibit:
			return R.drawable.type_mystery;
		case LetterboxHybrid:
			return R.drawable.type_letterbox;
		case LocationlessCache:
			return R.drawable.type_locationless;
		case MultiCache:
			return R.drawable.type_multi;
		case ProjectApeCache:
			return R.drawable.type_ape;
		case TraditionalCache:
			return R.drawable.type_traditional;
		case UnknownCache:
			return R.drawable.type_mystery;
		case VirtualCache:
			return R.drawable.type_virtual;
		case WebcamCache:
			return R.drawable.type_webcam;
		case WherigoCache:
			return R.drawable.type_wherigo;
		}
		return 0;
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
	
	protected Map<CacheType, List<SimpleGeocache>> cachesToCategories(SimpleGeocache[] caches) {
		Map<CacheType, List<SimpleGeocache>> result = new HashMap<CacheType, List<SimpleGeocache>>();
		
		for (SimpleGeocache cache : caches) {
			if (!result.containsKey(cache.getCacheType())) {
				result.put(cache.getCacheType(), new ArrayList<SimpleGeocache>());
			}
			
			result.get(cache.getCacheType()).add(cache);
		}
		return result;
	}
	
	protected SimpleGeocache[] downloadCaches(double latitude, double longitude) throws IOException {
		
		URL url = new URL(String.format((Locale)null, SERVICE_URL, latitude, longitude, account.encrypt(), ""));
		Log.i(TAG, "downloading " + url);
		
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
		account = Account.decrypt(dis.readUTF());
		if (account.getSession() != null && account.getSession().length() > 0) {
			Editor edit = prefs.edit();
			edit.putString("session", account.getSession());
			edit.commit();
		}
		
		// num of caches
		int cacheCount = dis.readInt();
		Log.i(TAG, "found caches: " + cacheCount);
		SimpleGeocache[] caches = new SimpleGeocache[cacheCount];
		for (int i = 0; i < cacheCount; i++) {
			caches[i] = SimpleGeocache.load(dis);
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
		
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		
		if (searchThread != null && !searchThread.isAlive())
			searchThread.start();		
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

	public void onProviderEnabled(String provider) {}

	public void onStatusChanged(String provider, int status, Bundle extras) {}
}