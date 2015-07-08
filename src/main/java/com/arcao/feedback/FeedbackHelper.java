package com.arcao.feedback;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.arcao.feedback.collector.AccountInfoCollector;
import com.arcao.feedback.collector.AppInfoCollector;
import com.arcao.feedback.collector.BuildConfigCollector;
import com.arcao.feedback.collector.Collector;
import com.arcao.feedback.collector.ConfigurationCollector;
import com.arcao.feedback.collector.ConstantsCollector;
import com.arcao.feedback.collector.DisplayManagerCollector;
import com.arcao.feedback.collector.LogCatCollector;
import com.arcao.feedback.collector.MemoryCollector;
import com.arcao.feedback.collector.SharedPreferencesCollector;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import timber.log.Timber;

public class FeedbackHelper {
	public static void sendFeedback(@NonNull Context context, @StringRes int resEmail, @StringRes int resSubject, @StringRes int resMessageText) {
		String subject = context.getString(resSubject, getApplicationName(context), getVersion(context));

		String email = context.getString(resEmail);

		Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE); // only e-mail apps
		intent.setType("plain/text");
		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, context.getString(resMessageText));
		intent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		try {
			createReport(context, FeedbackFileProvider.getReportFile(context));

			ArrayList<Uri> uris = new ArrayList<>();
			uris.add(FeedbackFileProvider.getReportFileUri());

			intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		} catch (IOException e) {
			Timber.e(e, e.getMessage());
		}

		context.startActivity(Intent.createChooser(intent, null));
	}

	private static void createReport(Context context, File reportFile) throws IOException {
		ZipOutputStream zos = null;

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
}
