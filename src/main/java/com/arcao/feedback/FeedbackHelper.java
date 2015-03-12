package com.arcao.feedback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import com.arcao.feedback.collector.*;
import org.apache.commons.io.IOUtils;
import timber.log.Timber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FeedbackHelper {
	private static final String REPORT_FILE = "%s-report.zip";

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
			Timber.e(e, e.getMessage());
		}

		context.startActivity(createEmailOnlyChooserIntent(context, intent, null));
	}

	private static File createReportFile(Context context) throws IOException {
		ZipOutputStream zos = null;

		File reportFile = getReportFile(context);

		if (reportFile.exists()) {
			Timber.d("Report file " + reportFile + " already exist.");
			if (reportFile.delete()) {
				Timber.d("Report file removed.");
			}
		}

		Timber.d("Creating report to " + reportFile);
		try {
			zos = new ZipOutputStream(new FileOutputStream(reportFile));
			writeCollectors(zos, context);
		}
		finally {
			IOUtils.closeQuietly(zos);
		}

		Timber.d("Report created.");

		return reportFile;
	}

	private static File getReportFile(Context context) {
		String reportFilename = String.format(REPORT_FILE, getApplicationName(context));
		return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), reportFilename);
	}

	private static void writeCollectors(ZipOutputStream zos, Context context) throws IOException {
		Collection<Collector> collectors = prepareCollectors(context);

		for (Collector collector : collectors) {
			zos.putNextEntry(new ZipEntry(collector.getName() + ".txt"));

			OutputStreamWriter writer = new OutputStreamWriter(zos, "UTF-8");
			writer.write(collector.toString());
			writer.flush();

			zos.closeEntry();
		}
	}

	private static Collection<Collector> prepareCollectors(Context context) {
		Collection<Collector> collectors = new ArrayList<>();

		collectors.add(new AppInfoCollector(context));
		collectors.add(new BuildConfigCollector());
		collectors.add(new ConfigurationCollector(context));
		collectors.add(new ConstantsCollector(Build.class, "BUILD"));
		collectors.add(new ConstantsCollector(Build.VERSION.class, "VERSION"));
		collectors.add(new MemoryCollector());
		collectors.add(new LogCatCollector(context));
		collectors.add(new SharedPreferencesCollector(context));
		collectors.add(new DisplayManagerCollector(context));
		collectors.add(new AccountInfoCollector(context));

		return collectors;
	}

	private static String getApplicationName(Context context) {
		int stringId = context.getApplicationInfo().labelRes;
		return context.getString(stringId);
	}

	private static String getVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Timber.e(e, e.getMessage());
			return "0.0";
		}
	}

	private static Intent createEmailOnlyChooserIntent(Context context, Intent source, CharSequence chooserTitle) {
		Vector<Intent> intents = new Stack<>();
		Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:info@domain.com"));
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
