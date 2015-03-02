package com.arcao.feedback.collector;

import org.acra.ACRAConstants;
import timber.log.Timber;

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
	protected String collect() {
		final StringBuilder memInfo = new StringBuilder();

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
				memInfo.append(line);
				memInfo.append("\n");
			}

		} catch (IOException e) {
			Timber.e(e, "MemoryCollector could not retrieve data");
		}

		return memInfo.toString();

	}
}
