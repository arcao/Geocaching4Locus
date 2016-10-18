package locus.api.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import locus.api.android.utils.LocusConst;
import locus.api.android.utils.exceptions.RequiredVersionMissingException;
import timber.log.Timber;

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
	 * Is external storage available for writing file?
	 * @return true if we can write to storage otherwise false
	 */
	private static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();

		// We can read and write the media
		// Something else is wrong. It may be one of many other states, but all we
		// need to know is we can neither read nor write
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	/**
	 * Get a path including file name to save data for Locus
	 * @param context	Context
	 * @return	path to file
	 */
	public static File getCacheFileName(Context context) {
		//if (!isExternalStorageWritable())
		//	throw new IllegalStateException("External storage (or SD Card) is not available.");

		File cacheFile = new File(Environment.getExternalStorageDirectory(), String.format("/Geocaching4Locus/%s", LOCUS_CACHE_FILENAME));

		Timber.d("Cache file for Locus: " + cacheFile.toString());

		File parentDirectory = cacheFile.getParentFile();

		parentDirectory.mkdirs();

		if (!parentDirectory.isDirectory())
			throw new IllegalStateException("External storage (or SD Card) is not writable.");


		return cacheFile;
	}

	/**
	 * Get a OutputFileStream to save data for Locus
	 * @param context Context
	 * @return OutputFileStream object for world readable file returned by getCacheFileName method
	 * @throws IOException If I/O error occurs
	 */
	@SuppressLint("SetWorldReadable")
	public static FileOutputStream getCacheFileOutputStream(Context context) throws IOException {
		File file = getCacheFileName(context);
		FileOutputStream fos = new FileOutputStream(file);
		fos.flush(); // create empty file
		if (!file.setReadable(true, false)) { // file has to be readable for Locus
			Timber.e("Unable to set readable all for: " + file);
		}

		return fos;
	}
}
