package menion.android.locus.addon.publiclib;

import android.content.Intent;

public class LocusIntentsPatch {	
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
}
