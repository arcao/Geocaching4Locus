package org.acra;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;

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
	public static void storeUserEmail(String userEmail) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Geocaching4LocusApplication.getAppContext());

		if (userEmail != null && userEmail.length() > 0) {
			prefs.edit().putString("acra.user.email", userEmail).commit();
		} else {
			prefs.edit().remove("acra.user.email").commit();
		}
	}
}
