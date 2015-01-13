package com.arcao.geocaching4locus.util.feedback.collector;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import org.acra.ACRA;
import org.acra.ACRAConstants;
import org.acra.util.PackageManagerWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.acra.ACRA.LOG_TAG;

public class LogCatCollector extends Collector {
	private final Context context;
	private final String bufferName = null;

	public LogCatCollector(Context context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "LOGCAT";
	}

	@Override
	public String collect() {
		final PackageManagerWrapper pm = new PackageManagerWrapper(context);

		if (!pm.hasPermission(Manifest.permission.READ_LOGS) && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			return "N/A";
		}

		final StringBuilder buffer = new StringBuilder();

		final int pid = android.os.Process.myPid();
		String pidPattern = null;
		if (pid > 0) {
			pidPattern = Integer.toString(pid) + "):";
		}

		final List<String> commandLine = new ArrayList<>();
		commandLine.add("logcat");
		if (bufferName != null) {
			commandLine.add("-b");
			commandLine.add(bufferName);
		}
		commandLine.add("-v");
		commandLine.add("time");

		try {
			final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			Log.d(LOG_TAG, "Retrieving logcat output...");

			// Dump stderr to null
			new Thread(new Runnable() {
				public void run() {
					try {
						InputStream stderr = process.getErrorStream();
						byte[] dummy = new byte[ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES];
						while (stderr.read(dummy) >= 0);
					} catch (IOException e) {
						// fall trough
					}
				}
			}).start();

			while (true) {
				final String line = bufferedReader.readLine();
				if (line == null) {
					break;
				}
				if (pidPattern == null || line.contains(pidPattern)) {
					buffer.append(line).append("\n");
				}
			}

		} catch (IOException e) {
			Log.e(ACRA.LOG_TAG, "LogCatCollector could not retrieve data.", e);
		}

		return buffer.toString();
	}
}
