package menion.android.locus.addon.publiclib;

import menion.android.locus.addon.publiclib.geoData.Point;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;

public class LocusIntents {

	private static final String TAG = "LocusIntents";
	
	/*
	   Add POI from your application
 	  -------------------------------
 	   - on places where is location needed, you may add link to your application. So for example, when
 	   you display Edit point dialog, you may see next to coordinates button "New". This is list of
 	   location sources and also place when your application appear with this method
 	   
 	   1. register intent-filter for your activity
 	   
       	<intent-filter>
          	<action android:name="menion.android.locus.GET_POINT" />
          	<category android:name="android.intent.category.DEFAULT" />
       	</intent-filter>
       
       2. register intent receiver in your application

		if (getIntent().getAction().equals(LocusConst.INTENT_GET_POINT)) {
   			// get some data here and finally return value back, more below
		}
	 */
	
	public static boolean isIntentGetLocation(Intent intent) {
		return intent != null && LocusConst.INTENT_GET_LOCATION.equals(intent.getAction());
	}
	
	public static boolean sendGetLocationData(Activity activity, 
			String name, double lat, double lon, double alt, double acc) {
		if (lat == 0.0 && lon == 0.0) {
			return false;
		} else {
			Intent intent = new Intent();
			// string value name
			intent.putExtra("name", name); // optional
			// rest are all DOUBLE values (to avoid problems even when for acc and alt isn't double needed)
			intent.putExtra("latitude", lat); // required, not 0.0
			intent.putExtra("longitude", lon); // required, not 0.0
			intent.putExtra("altitude", alt); // optional
			intent.putExtra("accuracy", acc); // optional
			activity.setResult(Activity.RESULT_OK, intent);
			activity.finish();
			return true;
		}
	}
	
	/*
	   Add action under point sub-menu
 	  -------------------------------
 	   - when you tap on any point on map or in Point screen, under last bottom button, are functions for 
 	   calling to some external application. Under this menu appear also your application
 	   
 	   1. register intent-filter for your activity
 	   
		<intent-filter>
      		<action android:name="menion.android.locus.ON_POINT_ACTION" />
      		<category android:name="android.intent.category.DEFAULT" />
   		</intent-filter>
       
       2. register intent receiver in your application

		if (getIntent().getAction().equals(LocusConst.INTENT_ON_POINT_ACTION)) {
   			double lat = getIntent().getDoubleExtra("latitude", 0.0);
   			double lon = getIntent().getDoubleExtra("longitude", 0.0);
   			double alt = getIntent().getDoubleExtra("altitude", 0.0);
   			double acc = getIntent().getDoubleExtra("accuracy", 0.0);
   
   			// do what you want with this ...
		}
	 */
	
	public static boolean isIntentOnPointAction(Intent intent) {
		return intent != null && LocusConst.INTENT_ON_POINT_ACTION.equals(intent.getAction());
	}
	
	public static Point handleIntentOnPointAction(Intent intent) 
			throws NullPointerException {
		// check source data
		if (intent == null)
			throw new NullPointerException("Intent cannot be null");
		// check intent itself
		if (!isIntentOnPointAction(intent)) {
			return null;
		}
		
		String name = intent.getStringExtra("name");
		Location loc;
		// in new version is already whole location as parcelable
		if (intent.getParcelableExtra("loc") != null) {
			loc = intent.getParcelableExtra("loc");
		} else {
			loc = new Location(TAG);
			loc.setLatitude(intent.getDoubleExtra("latitude", 0.0));
			loc.setLongitude(intent.getDoubleExtra("longitude", 0.0));
			loc.setAltitude(intent.getDoubleExtra("altitude", 0.0));
			loc.setAccuracy((float) intent.getDoubleExtra("accuracy", 0.0));
		}

		return new Point(name, loc);
	}
	
	/*
	   Add action under main function menu
 	  -------------------------------------
 	   - when you display menu->functions, your application appear here. Also you application (activity) may
 	    be added to right quick menu. Application will be called with current map center coordinates
 	   
 	   1. register intent-filter for your activity
 	   
		<intent-filter>
      		<action android:name="menion.android.locus.MAIN_FUNCTION" />
      		<category android:name="android.intent.category.DEFAULT" />
   		</intent-filter>
       
       2. register intent receiver in your application

		if (getIntent().getAction().equals(LocusConst.INTENT_MAIN_FUNCTION)) {
   			// more below ...
		}
	 */
	
	public static boolean isIntentMainFunction(Intent intent) {
		return intent != null && LocusConst.INTENT_MAIN_FUNCTION.equals(intent.getAction());
	}
	
	public interface OnIntentMainFunction {
		public void onLocationReceived(boolean gpsEnabled, Location locGps, Location locMapCenter);
		public void onFailed();
	}
	
	public static void handleIntentMainFunction(Intent intent, OnIntentMainFunction handler) 
			throws NullPointerException {
		// check source data
		if (intent == null)
			throw new NullPointerException("Intent cannot be null");
		if (handler == null)
			throw new NullPointerException("Handler cannot be null");
		// check intent itself
		if (!isIntentMainFunction(intent)) {
			handler.onFailed();
			return;
		}
		
		boolean gpsEnabled = intent.getBooleanExtra("gpsEnabled", false);
		handler.onLocationReceived(gpsEnabled,
				gpsEnabled ? (Location)intent.getParcelableExtra("locGps") : null,
				(Location) intent.getParcelableExtra("locCenter"));
	}
}
