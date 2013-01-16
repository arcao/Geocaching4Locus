package locus.api.android;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.RequiredVersionMissingException;
import locus.api.objects.extra.Waypoint;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

public class ActionDisplayPointsExtended extends ActionDisplayPoints {
	private static final String TAG = ActionDisplayPointsExtended.class.getName();
	private static final int FILE_VERSION = 2;
	
	public static boolean sendPack(Context context, PackWaypoints data, boolean callImport, int intentFlags)
			throws RequiredVersionMissingException {
		if (data == null)
			return false;
		Intent intent = new Intent();
		intent.addFlags(intentFlags);
		intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA, 
				data.getAsBytes());
		return sendData(LocusConst.ACTION_DISPLAY_DATA, context, intent, callImport);
	}
	
	public static boolean sendPacksFile(Context context, File file, boolean callImport, int intentFlags) throws RequiredVersionMissingException {
		return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA, context, file, callImport, intentFlags);
	}
	
	public static boolean sendPacksFile(String action, Context context, File file, boolean callImport, int intentFlags) throws RequiredVersionMissingException {
		if (!file.exists())
			return false;
		
		Intent intent = new Intent();
		intent.addFlags(intentFlags);
		intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.getAbsolutePath());
		
		return sendData(action, context, intent, callImport);
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
		File storageDirectory = null;
		if (isExternalStorageWritable()) {
			storageDirectory = Environment.getExternalStorageDirectory();
		} else {
			storageDirectory = context.getCacheDir();
		}
		
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
	
	public static void storeGeocacheToCache(Context context, Waypoint point) {
		File f = getGeocacheCacheFileName(context, point.gcData.getCacheID());
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
		
		DataOutputStream dos = null;
		
		try {
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));

			// write current version
			dos.writeInt(FILE_VERSION);
			point.write(dos);
			
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
		}
	}
	
	public static Waypoint loadGeocacheFromCache(Context context, String cacheCode) {
		File f = getGeocacheCacheFileName(context, cacheCode);
		
		if (f == null) {
			Log.e(TAG, "SD card isn't writeable!");
			return null;
		}
		
		if (!f.exists()) {
			Log.w(TAG, "Cache file not found: " + f);
			return null;
		}
		
		DataInputStream dis = null;
		
		try {
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			
			// check version
			if (FILE_VERSION != dis.readInt()) {
				return null;
			}

			Waypoint point = new Waypoint(dis); 			
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
		}
	}	
}
