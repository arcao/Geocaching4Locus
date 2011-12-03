package menion.android.locus.addon.publiclib;

import java.util.ArrayList;

import menion.android.locus.addon.publiclib.geoData.PointsData;
import menion.android.locus.addon.publiclib.utils.DataStorage;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DisplayDataExtended extends DisplayData {
	private static final String TAG = "DisplayDataExtended";
	
	/**
	 * Way how to send ArrayList<PointsData> object over intent to Locus. Data are
	 * stored in ContentProvider so don't forget to register it in manifest. This is
	 * recommended way how to send huge data to Locus. 
	 * @param data ArrayList of data that should be send to Locus
	 * @param uri URI to ContentProvider
	 * @return prepared Intent object
	 */
	public static Intent prepareDataCursor(ArrayList<PointsData> data, String uri) {
		if (data == null)
			return null;
		
		// set data
		DataStorage.setData(data);
		Intent intent = new Intent();
		intent.putExtra(LocusConst.EXTRA_POINTS_CURSOR_URI, uri);
		return intent;
	}
	
	/**
	 * Call Locus with specified Intent object. Intent must be prepared with a 
	 * {{@link #prepareDataCursor(ArrayList, String)} method. 
	 * @param context actual context
	 * @param intent Intent object with data for Locus
	 * @param callImport call import in Locus instead of show directly POIs on a map
	 * @return true if success
	 */
	public static boolean sendData(Context context, Intent intent, boolean callImport) {
		// really exist locus?
		if (!LocusUtils.isLocusAvailable(context)) {
			Log.w(TAG, "Locus is not installed");
			return false;
		}

		// check intent firstly
		if (intent == null || (intent.getParcelableArrayListExtra(LocusConst.EXTRA_POINTS_DATA_ARRAY) == null &&
				intent.getParcelableExtra(LocusConst.EXTRA_POINTS_DATA) == null &&
				intent.getStringExtra(LocusConst.EXTRA_POINTS_CURSOR_URI) == null)) {
			Log.w(TAG, "Intent 'null' or not contain any data");
			return false;
		}

		intent.putExtra(LocusConst.EXTRA_CALL_IMPORT, callImport);

		// create intent with right calling method
		intent.setAction(LocusConst.INTENT_DISPLAY_DATA);
		// finally start activity
		context.startActivity(intent);
		return true;
	}
}
