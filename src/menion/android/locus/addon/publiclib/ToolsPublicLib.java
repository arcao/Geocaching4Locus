package menion.android.locus.addon.publiclib;

import java.util.ArrayList;

import menion.android.locus.addon.publiclib.geoData.PointsData;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

public class ToolsPublicLib {

	private static final String TAG = "TAG";
	
	public static final String INTENT_DISPLAY_DATA = "android.intent.action.LOCUS_PUBLIC_LIB_DATA";
	
	public static final String EXTRA_POINTS_DATA = "EXTRA_POINTS_DATA";
	public static final String EXTRA_POINTS_DATA_ARRAY = "EXTRA_POINTS_DATA_ARRAY";
	
	public static boolean isLocusAvailable(Context context) {
	    try {
	        // set intent
	        final PackageManager packageManager = context.getPackageManager();
	        final Intent intent = new Intent(Intent.ACTION_VIEW);
	        intent.setData(Uri.parse("menion.points:x"));
	         
	        // return true or false
	        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public static boolean sendData(Context context, PointsData data) {
		if (data == null)
			return false;
		Intent intent = new Intent();
		intent.putExtra(EXTRA_POINTS_DATA, data);
		return sendData(context, intent);
	}
	
	public static boolean sendData(Context context, ArrayList<PointsData> data) {
		if (data == null)
			return false;
		Intent intent = new Intent();
		intent.putParcelableArrayListExtra(EXTRA_POINTS_DATA_ARRAY, data);
		return sendData(context, intent);
	}

	private static boolean sendData(Context context, Intent intent) {
		// really exist locus?
		if (!isLocusAvailable(context)) {
			Log.w(TAG, "Locus is not installed");
			return false;
		}
		
		// check intent firstly
		if (intent == null || (intent.getParcelableArrayListExtra(EXTRA_POINTS_DATA_ARRAY) == null && 
				intent.getParcelableExtra(EXTRA_POINTS_DATA) == null)) {
			Log.w(TAG, "Intent 'null' or not contain any data");
			return false;
		}
		
		// create intent with right calling method
		intent.setAction(INTENT_DISPLAY_DATA);
		// finally start activity
		context.startActivity(intent);
		return true;
	}
}
