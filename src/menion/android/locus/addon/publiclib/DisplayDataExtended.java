package menion.android.locus.addon.publiclib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import menion.android.locus.addon.publiclib.geoData.Point;
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
	 * @param file
	 * @return
	 */
	public static Intent prepareDataFile(ArrayList<PointsData> data, File file) {
		if (data == null || data.size() == 0)
			return null;
		
		FileOutputStream os = null;
		DataOutputStream dos = null;
		try {
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
			Log.e(TAG, "saveBytesInstant(" + file + ", " + data + ")", e);
			return null;
		} finally {
			try {
				if (dos != null) {
					dos.close();
				}
			} catch (Exception e) {
				Log.e(TAG, "saveBytesInstant(" + file + ", " + data + ")", e);
			}
		}
		
		// store data to file
		Intent intent = new Intent();
		intent.putExtra(LocusConst.EXTRA_POINTS_FILE_PATH, file.getAbsolutePath());
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
	 * @return	path to file 
	 */
	public static File getCacheFileName(Context context) {
		if (!isExternalStorageWritable())
			return context.getCacheDir();
		
		File storageDirectory = Environment.getExternalStorageDirectory();
		File cacheFile = new File(storageDirectory, String.format("/Android/data/%s/cache/data.locus", context.getPackageName()));
		cacheFile.getParentFile().mkdirs();
		
		return cacheFile;
	}
	
	/**
	 * Get a path including file name to save geocache for later use
	 * @param context	Context
	 * @return	path to file 
	 */
	public static File getGeocacheCacheFileName(Context context, String cacheCode) {
		if (!isExternalStorageWritable())
			return new File(context.getCacheDir(), cacheCode + ".locus");
		
		File storageDirectory = Environment.getExternalStorageDirectory();
		File cacheFile = new File(storageDirectory, String.format("/Android/data/%s/cache/%s.locus", context.getPackageName(), cacheCode));
		cacheFile.getParentFile().mkdirs();
		
		return cacheFile;
	}	
	
	public static void storeGeocacheToCache(Context context, Point point) {
		File f = getGeocacheCacheFileName(context, point.getGeocachingData().cacheID);
		if (f == null) {
			Log.e(TAG, "SD card isn't writeable!");
			return;
		}
		
		if (f.exists()) {
			if (!f.delete()) {
				Log.e(TAG, "Geocache cache can't be deleted!");
				return;
			}
		}
		
		Parcel p = Parcel.obtain();
		DataOutputStream dos = null;
		
		try {
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));

			p.writeParcelable(point, 0);
			
			byte[] byteData = p.marshall();
			
			// write current version
			dos.writeInt(FILE_VERSION);
			
			dos.writeInt(byteData.length);
			dos.write(byteData);
			
			dos.flush();
			dos.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {}
			}
			p.recycle();
		}
	}
	
	public static Point loadGeocacheFromCache(Context context, String cacheCode) {
		File f = getGeocacheCacheFileName(context, cacheCode);
		
		if (f == null) {
			Log.e(TAG, "SD card isn't writeable!");
			return null;
		}
		
		if (!f.exists()) {
			Log.w(TAG, "Cache file not found: " + f);
			return null;
		}
		
		Parcel p = Parcel.obtain();
		DataInputStream dis = null;
		
		try {
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			
			// check version
			if (FILE_VERSION != dis.readInt()) {
				return null;
			}
			
			int size = dis.readInt();
			byte[] byteData = new byte[size];
			dis.read(byteData);
			
			p.unmarshall(byteData, 0, size);
			p.setDataPosition(0);
			
			Point point = p.readParcelable(Point.class.getClassLoader());
			
			// fix null pointer exception
			if (point != null && point.getGeocachingData() != null && point.getGeocachingData().shortDescription == null) {
				point.getGeocachingData().shortDescription = "";
			}
			
			return point;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
			return null;
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {}
			}
			p.recycle();
		}
	}
	
	private static boolean hasData(Intent intent) {
		if (intent == null)
			return false;
		
		return !(
				intent.getParcelableArrayListExtra(LocusConst.EXTRA_POINTS_DATA_ARRAY) == null && 
				intent.getParcelableExtra(LocusConst.EXTRA_POINTS_DATA) == null &&
				intent.getStringExtra(LocusConst.EXTRA_POINTS_CURSOR_URI) == null && 
				intent.getStringExtra(LocusConst.EXTRA_POINTS_FILE_PATH) == null &&
				intent.getParcelableExtra(LocusConst.EXTRA_TRACKS_SINGLE) == null &&
				intent.getParcelableArrayListExtra(LocusConst.EXTRA_TRACKS_MULTI) == null);
	}
}
