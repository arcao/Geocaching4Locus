package com.arcao.feedback.collector;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import org.acra.util.PackageManagerWrapper;
import org.apache.commons.io.IOUtils;
import timber.log.Timber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogCatCollector extends Collector {
	private static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 8192;

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
	protected String collect() {
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
		commandLine.add("-t");
		commandLine.add("1000");
		commandLine.add("-v");
		commandLine.add("time");

		BufferedReader bufferedReader = null;

		try {
			final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
			bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			Timber.d("Retrieving logcat output...");
			// Dump stderr to null
			new Thread(new Runnable() {
				public void run() {
					InputStream stderr = process.getErrorStream();
					try {
						byte[] dummy = new byte[DEFAULT_BUFFER_SIZE_IN_BYTES];
						while (stderr.read(dummy) >= 0);
					} catch (IOException e) {
						// fall trough
					} finally {
						IOUtils.closeQuietly(stderr);
					}
				}
			}).start();

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (pidPattern == null || line.contains(pidPattern)) {
					buffer.append(line).append("\n");
				}
			}

		} catch (IOException e) {
			Timber.e("LogCatCollector could not retrieve data.");
		} finally {
			IOUtils.closeQuietly(bufferedReader);
		}

		return buffer.toString();
	}
}
