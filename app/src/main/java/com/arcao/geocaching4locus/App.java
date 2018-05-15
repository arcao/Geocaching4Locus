package com.arcao.geocaching4locus;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.arcao.geocaching4locus.authentication.util.Account;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.authentication.util.PreferenceAccountManager;
import com.arcao.geocaching4locus.base.constants.CrashlyticsConstants;
import com.arcao.geocaching4locus.base.util.CrashlyticsTree;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import locus.api.android.utils.LocusUtils;
import timber.log.Timber;

public class App extends Application {
    private AccountManager accountManager;
    private String deviceId;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().build());
        }

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);
        Timber.plant(new CrashlyticsTree());

        Crashlytics.setUserIdentifier(getDeviceId());

        accountManager = new PreferenceAccountManager(this);

        Account account = accountManager.getAccount();
        if (account != null) {
            Crashlytics.setBool(CrashlyticsConstants.PREMIUM_MEMBER, account.premium());
        }

        try {
            LocusUtils.LocusVersion lv = LocusUtils.getActiveVersion(this);
            if (lv != null) {
                Crashlytics.setString(CrashlyticsConstants.LOCUS_VERSION, lv.getVersionName());
                Crashlytics.setString(CrashlyticsConstants.LOCUS_PACKAGE, lv.getPackageName());
            } else {
                Crashlytics.setString(CrashlyticsConstants.LOCUS_VERSION, "");
                Crashlytics.setString(CrashlyticsConstants.LOCUS_PACKAGE, "");
            }
        } catch (Throwable t) {
            Timber.e(t);
        }
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public String getDeviceId() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        if (deviceId == null)
            deviceId = pref.getString("device_id", null);

        if (TextUtils.isEmpty(deviceId)) {
            deviceId = UUID.randomUUID().toString();
            pref.edit().putString("device_id", deviceId).apply();
        }

        return deviceId;
    }

    public String getVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Timber.e(e);
            return "1.0";
        }
    }

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

        if (cookies == null)
            return;

        for (String cookie : cookies.split(";")) {
            String[] cookieParts = cookie.split("=");
            if (cookieParts.length > 0) {
                String newCookie = cookieParts[0].trim() + "=;expires=Sat, 1 Jan 2000 00:00:01 UTC;";
                cookieManager.setCookie(domain, newCookie);
            }
        }
    }
}
