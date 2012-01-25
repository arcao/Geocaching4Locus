package menion.android.locus.addon.publiclib;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import menion.android.locus.addon.publiclib.geoData.PointsData;
import menion.android.locus.addon.publiclib.utils.DataStorage;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Parcel;
import android.util.Log;

public class DisplayDataExtended extends DisplayData {
	private static final String TAG = "DisplayDataExtended";
	private static final int FILE_VERSION = 1;
	
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
	 * Allow to send data to locus, by storing serialized version of data into file. This method
	 * can have advantage over cursor in simplicity of implementation and also that filesize is
	 * not limited as in Cursor method. On second case, need permission for disk access and should
	 * be slower due to IO operations. Be careful about size of data. This method can cause OutOfMemory
	 * error on Locus side if data are too big
	 *   
	 * @param data
	 * @param filepath
	 * @return
	 */
	public static Intent prepareDataFile(ArrayList<PointsData> data, String filepath) {
		if (data == null || data.size() == 0)
			return null;
		
		FileOutputStream os = null;
		DataOutputStream dos = null;
		try {
			File file = new File(filepath);
			file.getParentFile().mkdirs();

			if (file.exists())
				file.delete();
			if (!file.exists()) {
				file.createNewFile();
			}

			os = new FileOutputStream(file, false);
			dos = new DataOutputStream(os);
	
			// write current version
			dos.writeInt(FILE_VERSION);
			
			// write data
			for (int i = 0; i < data.size(); i++) {
				// get byte array
				Parcel par = Parcel.obtain();
				data.get(i).writeToParcel(par, 0);
				byte[] byteData = par.marshall();
				
				// write data
				dos.writeInt(byteData.length);
				dos.write(byteData);
			}
				
			os.flush();
		} catch (Exception e) {
			Log.e(TAG, "saveBytesInstant(" + filepath + ", " + data + ")", e);
			return null;
		} finally {
			try {
				if (dos != null) {
					dos.close();
				}
			} catch (Exception e) {
				Log.e(TAG, "saveBytesInstant(" + filepath + ", " + data + ")", e);
			}
		}
		
		// store data to file
		Intent intent = new Intent();
		intent.putExtra(LocusConst.EXTRA_POINTS_FILE_PATH, filepath);
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
		if (!hasData(intent)) {
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
	
	/**
	 * Is external storage available for writing file?
	 * @return true if we can write to storage otherwise false
	 */
	public static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			return true;
		} else {
			// Something else is wrong. It may be one of many other states, but all we
			// need
			// to know is we can neither read nor write
			return false;
		}
	}
	
	/**
	 * Get a path including file name to save data for Locus
	 * @param context	Context
	 * @return	path to file to save data for Locus 
	 */
	public static String getCacheFileName(Context context) {
		if (!isExternalStorageWritable())
			return null;
		
		File storageDirectory = Environment.getExternalStorageDirectory();
		File cacheFile = new File(storageDirectory, String.format("/Android/data/%s/cache/data.locus", context.getPackageName()));
		cacheFile.getParentFile().mkdirs();
		
		return cacheFile.getAbsolutePath();
	}
	
	private static boolean hasData(Intent intent) {
		if (intent == null)
			return false;
		
		return !(
				intent.getParcelableArrayListExtra(LocusConst.EXTRA_POINTS_DATA_ARRAY) == null && 
				intent.getParcelableExtra(LocusConst.EXTRA_POINTS_DATA) == null &&
				intent.getStringExtra(LocusConst.EXTRA_POINTS_CURSOR_URI) == null && 
				intent.getStringExtra(LocusConst.EXTRA_POINTS_FILE_PATH) == null &&
				intent.getParcelableExtra(LocusConst.EXTRA_TRACKS_SINGLE) == null);
	}
}
