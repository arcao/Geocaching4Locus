package com.arcao.geocaching4locus;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.authentication.helper.PreferenceAuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.util.LocusTesting;
import locus.api.android.utils.LocusUtils;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.scribe.model.Token;
import timber.log.Timber;

import java.util.UUID;

@ReportsCrashes(
		buildConfigClass = BuildConfig.class,
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
public class App extends android.app.Application {
	private AuthenticatorHelper mAuthenticatorHelper;
	private String mDeviceId;

	@Override
	public void onCreate() {
		super.onCreate();

		Timber.plant(new Timber.DebugTree());
		ACRA.init(this);

	 	mAuthenticatorHelper = new PreferenceAuthenticatorHelper(this);

		if (mAuthenticatorHelper.hasAccount()) {
			ACRA.getErrorReporter().putCustomData("userName", mAuthenticatorHelper.getAccount().name);
		}

		try {
			LocusUtils.LocusVersion lv = LocusTesting.getActiveVersion(this);
			if (lv != null) {
				ACRA.getErrorReporter().putCustomData("LocusVersion", lv.versionName);
				ACRA.getErrorReporter().putCustomData("LocusPackage", lv.packageName);
			}
		} catch (Throwable t) {
			Timber.e(t, t.getMessage());
			ACRA.getErrorReporter().putCustomData("LocusVersion", "failed");
			ACRA.getErrorReporter().putCustomData("LocusPackage", "failed");
		}

		System.setProperty("debug", "1");
	}

	public static App get(Context context) {
		return (App) context.getApplicationContext();
	}

	public AuthenticatorHelper getAuthenticatorHelper() {
		return mAuthenticatorHelper;
	}

	public String getDeviceId() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

		if (mDeviceId == null) {
			mDeviceId = pref.getString("device_id", null);
		}

		if (mDeviceId == null) {
			mDeviceId = UUID.randomUUID().toString();
			pref.edit().putString("device_id", mDeviceId).apply();
		}

		return mDeviceId;
	}

	public String getVersion() {
		try {
			return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Timber.e(e, e.getMessage());
			return "1.0";
		}
	}

	public void storeOAuthToken(Token token) {
		if (token == null || token.isEmpty())
			return;

		SharedPreferences pref = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);

		pref.edit()
			.putString(PrefConstants.OAUTH_TOKEN, token.getToken())
			.putString(PrefConstants.OAUTH_TOKEN_SECRET, token.getSecret())
			.apply();
	}

	public Token getOAuthToken() {

		SharedPreferences prefs = getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);
		return new Token(
			prefs.getString(PrefConstants.OAUTH_TOKEN, ""),
			prefs.getString(PrefConstants.OAUTH_TOKEN_SECRET, "")
		);
	}

	@SuppressWarnings("deprecation")
	public void clearGeocachingCookies() {
		// required to call
		CookieSyncManager.createInstance(this).sync();

		// setCookie acts differently when trying to expire cookies between builds of Android that are using
		// Chromium HTTP stack and those that are not. Using both of these domains to ensure it works on both.
		clearCookiesForDomain("geocaching.com");
		clearCookiesForDomain(".geocaching.com");
		clearCookiesForDomain("https://geocaching.com");
		clearCookiesForDomain("https://.geocaching.com");

		CookieSyncManager.createInstance(this).sync();
	}

	private static void clearCookiesForDomain(String domain) {
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
	}
}
