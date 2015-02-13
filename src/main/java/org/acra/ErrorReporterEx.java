package org.acra;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Extension class for ACRA ErrorReporter class.
 *
 * @author arcao
 *
 */
public class ErrorReporterEx {
	/**
	 * Store user comment
	 *
	 * @param comment
	 *          user comment
	 */
	public static void storeUserComment(String comment) {
		ACRA.getErrorReporter().putCustomData("USER_COMMENT", comment);
	}

	/**
	 * Store user e-mail to ErrorReporter crash properties.
	 *
	 * @param userEmail
	 *          user e-mail
	 */
	public static void storeUserEmail(Context context, String userEmail) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (userEmail != null && userEmail.length() > 0) {
			prefs.edit().putString("acra.user.email", userEmail).apply();
		} else {
			prefs.edit().remove("acra.user.email").apply();
		}
	}
}
