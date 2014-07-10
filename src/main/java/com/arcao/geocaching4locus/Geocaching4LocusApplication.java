package com.arcao.geocaching4locus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.authentication.helper.PreferenceAuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.util.UUID;

import locus.api.android.utils.LocusUtils;
import oauth.signpost.OAuthConsumer;

@ReportsCrashes(
		formKey =  AppConstants.ERROR_FORM_KEY,
		formUri = AppConstants.ERROR_SCRIPT_URL,
		mode = ReportingInteractionMode.NOTIFICATION,
		resNotifTickerText = R.string.crash_notif_ticker_text,
		resNotifTitle = R.string.crash_notif_title,
		resNotifText = R.string.crash_notif_text,
		resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
		resDialogText = R.string.crash_dialog_text,
		resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
		resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
		resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
		resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class Geocaching4LocusApplication extends android.app.Application {
	private static final String TAG = "G4L|Geocaching4LocusApplication";

	private static Context context;
	private static AuthenticatorHelper authenticatorHelper;
	private static String deviceId;

	@Override
	public void onCreate() {
		context = getApplicationContext();

		disableConnectionReuseIfNecessary();

		// The following line triggers the initialization of ACRA
		ACRA.init(this);

	 	authenticatorHelper = new PreferenceAuthenticatorHelper(this);
		authenticatorHelper.convertFromOldStorage();

		if (authenticatorHelper.hasAccount()) {
			ACRA.getErrorReporter().putCustomData("userName", authenticatorHelper.getAccount().name);
		}

		try {
			LocusUtils.LocusVersion lv = LocusUtils.getActiveVersion(this);
			if (lv != null) {
				ACRA.getErrorReporter().putCustomData("LocusVersion", lv.versionName);
				ACRA.getErrorReporter().putCustomData("LocusPackage", lv.packageName);
			}
		} catch (Throwable t) {
			Log.e(TAG, t.getMessage(), t);
			ACRA.getErrorReporter().putCustomData("LocusVersion", "failed");
			ACRA.getErrorReporter().putCustomData("LocusPackage", "failed");
		}

		System.setProperty("debug", "1");

		super.onCreate();
	}

	public static Context getAppContext() {
		return context;
	}

	public static AuthenticatorHelper getAuthenticatorHelper() {
		return authenticatorHelper;
	}

	public static String getDeviceId() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		if (deviceId == null) {
			deviceId = pref.getString("device_id", null);
		}

		if (deviceId == null) {
			deviceId = UUID.randomUUID().toString();
			pref.edit().putString("device_id", deviceId).commit();
		}

		return deviceId;
	}

	public static String getVersion() {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
			return "1.0";
		}
	}

	/**
	 * Some lowend phones can kill the app so if is necessary we must temporary store Token and Token secret
	 * @param consumer consumer object with valid Token and Token secret
	 */
	public static void storeRequestTokens(OAuthConsumer consumer) {
		if (consumer.getToken() == null || consumer.getTokenSecret() == null)
			return;

		SharedPreferences pref = context.getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);

		pref.edit()
			.putString(PrefConstants.OAUTH_TOKEN, consumer.getToken())
			.putString(PrefConstants.OAUTH_TOKEN_SECRET, consumer.getTokenSecret())
			.commit();
	}

	/**
	 * Some lowend phones can kill the app so if is necessary we must load temporary saved tokens back to consumer
	 * @param consumer consumer object where will be Token and Token secret stored
	 */
	public static void loadRequestTokensIfNecessary(OAuthConsumer consumer) {
		if (consumer.getToken() != null && consumer.getTokenSecret() != null)
			return;

		SharedPreferences pref = context.getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);
		consumer.setTokenWithSecret(
				pref.getString(PrefConstants.OAUTH_TOKEN, null),
				pref.getString(PrefConstants.OAUTH_TOKEN_SECRET, null)
		);
	}

	public static void clearGeocachingCookies() {
		// setCookie acts differently when trying to expire cookies between builds of Android that are using
		// Chromium HTTP stack and those that are not. Using both of these domains to ensure it works on both.
		clearCookiesForDomain(context, "geocaching.com");
		clearCookiesForDomain(context, ".geocaching.com");
		clearCookiesForDomain(context, "https://geocaching.com");
		clearCookiesForDomain(context, "https://.geocaching.com");
	}

	private static void clearCookiesForDomain(Context context, String domain) {
		// This is to work around a bug where CookieManager may fail to instantiate if CookieSyncManager
		// has never been created.
		CookieSyncManager syncManager = CookieSyncManager.createInstance(context);
		syncManager.sync();

		CookieManager cookieManager = CookieManager.getInstance();

		String cookies = cookieManager.getCookie(domain);
		if (cookies == null) {
			return;
		}

		String[] splitCookies = cookies.split(";");
		for (String cookie : splitCookies) {
			String[] cookieParts = cookie.split("=");
			if (cookieParts.length > 0) {
				String newCookie = cookieParts[0].trim() + "=;expires=Sat, 1 Jan 2000 00:00:01 UTC;";
				cookieManager.setCookie(domain, newCookie);
			}
		}
		cookieManager.removeExpiredCookie();
	}

	private static void disableConnectionReuseIfNecessary() {
		// HTTP connection reuse which was buggy pre-froyo
		if (VERSION.SDK_INT < VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}
}
