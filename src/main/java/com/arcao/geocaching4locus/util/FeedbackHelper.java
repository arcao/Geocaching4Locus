package com.arcao.menza.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import com.arcao.geocaching4locus.util.feedback.collector.*;

import java.io.*;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FeedbackHelper {
	private static final String TAG = "FeedbackHelper";

	private static final String REPORT_FILE = "report.zip";

	public static void sendFeedback(Context context, int resEmail, int resSubject, int resMessageText) {
		String subject = context.getString(resSubject, getApplicationName(context), getVersion(context));

		String email = context.getString(resEmail);

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, context.getString(resMessageText));
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(createReportFile(context)));
		} catch (IOException e) {
			// do nothing
		}

		context.startActivity(createEmailOnlyChooserIntent(context, intent, null));
	}

	private static File createReportFile(Context context) throws IOException {
		ZipOutputStream zos = null;

		try {
			zos = new ZipOutputStream(getCacheFileOutputStream(context, REPORT_FILE));

			zos.putNextEntry(new ZipEntry("info.txt"));
			writeCollectorContent(zos, context);
			zos.closeEntry();

			zos.putNextEntry(new ZipEntry("log.txt"));
			writeLogContent(zos);
			zos.closeEntry();
		}
		finally {
			if (zos != null) zos.close();
		}

		return getCacheFileName(context, REPORT_FILE);
	}

	private static void writeCollectorContent(OutputStream outputStream, Context context) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");

		writer.write(new AppInfoCollector(context).toString());
		writer.write(new ConfigurationCollector(context).toString());
		writer.write(new ConstantsCollector(Build.class, "BUILD").toString());
		writer.write(new ConstantsCollector(Build.VERSION.class, "VERSION").toString());
		writer.write(new MemoryCollector().toString());
		writer.write(new LogCatCollector(context).toString());

		writer.close();
	}

	private static void writeLogContent(OutputStream outputStream) {

	}


	/**
	 * Get a path including file name to save data
	 * @param context	Context
	 * @return	path to file
	 * @throws java.io.IOException If external storage isn't available or writable
	 */
	public static File getCacheFileName(Context context, String filename) throws IOException {
		File cacheFile = context.getFileStreamPath(filename);
		Log.d(TAG, "Cache file for Locus: " + cacheFile.toString());

		return cacheFile;
	}

	/**
	 * Get a OutputFileStream to save data
	 * @param context Context
	 * @return OutputFileStream object for world readable file returned by getCacheFileName method
	 * @throws IOException If I/O error occurs
	 */
	public static FileOutputStream getCacheFileOutputStream(Context context, String filename) throws IOException {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			return context.openFileOutput(filename, Context.MODE_WORLD_READABLE); // file has to be readable for external APP
		} else {
			File file = getCacheFileName(context, filename);
			FileOutputStream fos = new FileOutputStream(file);
			fos.flush(); // create empty file
			file.setReadable(true, false); // file has to be readable for external APP

			return fos;
		}
	}

	public static String getApplicationName(Context context) {
		int stringId = context.getApplicationInfo().labelRes;
		return context.getString(stringId);
	}

	public static String getVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			return "0.0";
		}
	}

	protected static Intent createEmailOnlyChooserIntent(Context context, Intent source, CharSequence chooserTitle) {
		Vector<Intent> intents = new Stack<>();
		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "info@domain.com", null));
		List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(i, 0);

		for(ResolveInfo ri : activities) {
			Intent target = new Intent(source);
			target.setPackage(ri.activityInfo.packageName);
			intents.add(target);
		}

		if(!intents.isEmpty()) {
			Intent chooserIntent = Intent.createChooser(intents.remove(0), chooserTitle);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Parcelable[intents.size()]));

			return chooserIntent;
		} else {
			return Intent.createChooser(source, chooserTitle);
		}
	}
}
