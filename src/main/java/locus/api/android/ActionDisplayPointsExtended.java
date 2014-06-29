package locus.api.android;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import locus.api.android.objects.PackWaypoints;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;

public class ActionDisplayPointsExtended extends ActionDisplayPoints {
	private static final String LOCUS_CACHE_FILENAME = "data.locus";

	private static final String TAG = ActionDisplayPointsExtended.class.getName();

	public static boolean sendPack(Context context, PackWaypoints data, boolean callImport, boolean center, int intentFlags)
			throws RequiredVersionMissingException {
		if (data == null)
			return false;
		Intent intent = new Intent();
		intent.addFlags(intentFlags);
		intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA,
				data.getAsBytes());
		return sendData(LocusConst.ACTION_DISPLAY_DATA, context, intent, callImport, center);
	}

	public static boolean sendPacksFile(Context context, File file, boolean callImport, boolean center, int intentFlags) throws RequiredVersionMissingException {
		return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA, context, file, callImport, center, intentFlags);
	}

	public static boolean sendPacksFile(String action, Context context, File file, boolean callImport, boolean center, int intentFlags) throws RequiredVersionMissingException {
		if (!file.exists())
			return false;

		Intent intent = new Intent();
		intent.addFlags(intentFlags);
		intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.getAbsolutePath());

		return sendData(action, context, intent, callImport, center);
	}

	/**
	 * Get a path including file name to save data for Locus
	 * @param context	Context
	 * @return	path to file
	 * @throws IOException If external storage isn't available or writable
	 */
	public static File getCacheFileName(Context context) throws IOException {
		File cacheFile = context.getFileStreamPath(LOCUS_CACHE_FILENAME);
		Log.d(TAG, "Cache file for Locus: " + cacheFile.toString());

		return cacheFile;
	}

	/**
	 * Get a OutputFileStream to save data for Locus
	 * @param context Context
	 * @return OutputFileStream object for world readable file returned by getCacheFileName method
	 * @throws IOException If I/O error occurs
	 */
	public static FileOutputStream getCacheFileOutputStream(Context context) throws IOException {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return context.openFileOutput(LOCUS_CACHE_FILENAME, Context.MODE_WORLD_READABLE); // file has to be readable for Locus
		} else {
			File file = getCacheFileName(context);
			FileOutputStream fos = new FileOutputStream(file);
			fos.flush(); // create empty file
			file.setReadable(true, false); // file has to be readable for Locus

			return fos;
		}
	}
}
