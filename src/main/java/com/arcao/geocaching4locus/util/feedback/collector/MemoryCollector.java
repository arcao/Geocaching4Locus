package com.arcao.geocaching4locus.util.feedback.collector;

import android.util.Log;
import org.acra.ACRAConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MemoryCollector extends Collector {

	@Override
	public String getName() {
		return "MEMORY";
	}

	@Override
	public String collect() {
		final StringBuilder meminfo = new StringBuilder();

		try {
			final List<String> commandLine = new ArrayList<String>();
			commandLine.add("dumpsys");
			commandLine.add("meminfo");
			commandLine.add(Integer.toString(android.os.Process.myPid()));

			final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
			final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), ACRAConstants.DEFAULT_BUFFER_SIZE_IN_BYTES);

			while (true) {
				final String line = bufferedReader.readLine();
				if (line == null) {
					break;
				}
				meminfo.append(line);
				meminfo.append("\n");
			}

		} catch (IOException e) {
			Log.e("", "MemoryCollector could not retrieve data", e);
		}

		return meminfo.toString();

	}
}
