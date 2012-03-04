package com.arcao.geocaching4locus.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Throwables {
	/**
	 * Get a stack trace of Throwable as a string representation. 
	 * @param t throwable
	 * @return stack trace
	 */
	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	/**
	 * Read cat log and return the log. To call method the permission <b>android.permission.READ_LOGS</b> is required! 
	 * @return log from catlog
	 */
	public static String readLogCat() {
		Process mLogcatProc = null;
		BufferedReader reader = null;
		
		try {
			mLogcatProc = Runtime.getRuntime().exec(new String[] {
					"logcat",
					"-d",
					"AndroidRuntime:E [Your Log Tag Here]:V *:S"
			});

			reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()));

			String line;
			final StringBuilder log = new StringBuilder();
			String separator = System.getProperty("line.separator");

			while ((line = reader.readLine()) != null) {
				log.append(line);
				log.append(separator);
			}

			return log.toString();
		} catch (IOException e) {

			return "";
		} finally {
			if (reader != null)
				try { reader.close(); } catch (IOException e) {	}
			if (mLogcatProc != null)
				mLogcatProc.destroy();
		}
	}
}
