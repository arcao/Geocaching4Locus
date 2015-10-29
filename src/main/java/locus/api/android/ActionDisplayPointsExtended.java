package locus.api.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import timber.log.Timber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ActionDisplayPointsExtended extends ActionDisplayPoints {
	private static final String LOCUS_CACHE_FILENAME = "data.locus";

	public static boolean sendPacksFile(Context context, File file, boolean callImport, boolean center, int intentFlags) throws RequiredVersionMissingException {
		return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA, context, file, callImport, center,
				intentFlags);
	}

	private static boolean sendPacksFile(String action, Context context, File file,
			boolean callImport, boolean center, int intentFlags) throws RequiredVersionMissingException {
		if (!file.exists())
			return false;

		Intent intent = new Intent();
		intent.addFlags(intentFlags);
		intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.getAbsolutePath());

		return sendData(action, context, intent, callImport, center);
	}

	@Nullable
	public static Intent createSendPacksIntent(File file, boolean callImport, boolean center) {
		if (!file.exists())
			return null;

		Intent intent = new Intent(LocusConst.ACTION_DISPLAY_DATA);

		intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.getAbsolutePath());

		// set centering tag
		intent.putExtra(LocusConst.INTENT_EXTRA_CENTER_ON_DATA, center);

		// set import tag
		intent.putExtra(LocusConst.INTENT_EXTRA_CALL_IMPORT, callImport);

		return intent;
	}

	/**
	 * Get a path including file name to save data for Locus
	 * @param context	Context
	 * @return	path to file
	 */
	public static File getCacheFileName(Context context) {
		File cacheFile = context.getFileStreamPath(LOCUS_CACHE_FILENAME);
		Timber.d("Cache file for Locus: " + cacheFile.toString());

		return cacheFile;
	}

	/**
	 * Get a OutputFileStream to save data for Locus
	 * @param context Context
	 * @return OutputFileStream object for world readable file returned by getCacheFileName method
	 * @throws IOException If I/O error occurs
	 */
	@SuppressLint("WorldReadableFiles")
	public static FileOutputStream getCacheFileOutputStream(Context context) throws IOException {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			//noinspection deprecation
			return context.openFileOutput(LOCUS_CACHE_FILENAME, Context.MODE_WORLD_READABLE); // file has to be readable for Locus
		} else {
			File file = getCacheFileName(context);
			FileOutputStream fos = new FileOutputStream(file);
			fos.flush(); // create empty file
			if (!file.setReadable(true, false)) { // file has to be readable for Locus
				Timber.e("Unable to set readable all for: " + file);
			}

			return fos;
		}
	}
}
