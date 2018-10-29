package com.arcao.geocaching4locus

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.StrictMode
import android.preference.PreferenceManager
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.authentication.util.PreferenceAccountManager
import com.arcao.geocaching4locus.base.constants.CrashlyticsConstants
import com.arcao.geocaching4locus.base.util.AnalyticsUtil
import com.arcao.geocaching4locus.base.util.CrashlyticsTree
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import locus.api.android.utils.LocusUtils
import timber.log.Timber
import java.util.*

class App : Application() {
    val accountManager: AccountManager by lazy {
        PreferenceAccountManager(this)
    }

    val deviceId: String by lazy {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        var value = pref.getString("device_id", null)

        if (value.isNullOrEmpty()) {
            value = UUID.randomUUID().toString()
            pref.edit().putString("device_id", value).apply()
        }
        value
    }

    val version: String by lazy {
        try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: NameNotFoundException) {
            Timber.e(e)
            "1.0"
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().build())
        }

        // Set up Crashlytics, disabled for debug builds
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit)
        Timber.plant(CrashlyticsTree())

        Crashlytics.setUserIdentifier(deviceId)

        val account = accountManager.account
        if (account != null) {
            Crashlytics.setBool(CrashlyticsConstants.PREMIUM_MEMBER, account.premium())
            AnalyticsUtil.setPremiumUser(this, account.premium())
        }

        try {
            val lv = LocusUtils.getActiveVersion(this)
            if (lv != null) {
                Crashlytics.setString(CrashlyticsConstants.LOCUS_VERSION, lv.versionName)
                Crashlytics.setString(CrashlyticsConstants.LOCUS_PACKAGE, lv.packageName)
            } else {
                Crashlytics.setString(CrashlyticsConstants.LOCUS_VERSION, "")
                Crashlytics.setString(CrashlyticsConstants.LOCUS_PACKAGE, "")
            }
        } catch (t: Throwable) {
            Timber.e(t)
        }

    }

    fun clearGeocachingCookies() {
        // required to call
        CookieSyncManager.createInstance(this).sync()

        // setCookie acts differently when trying to expire cookies between builds of Android that are using
        // Chromium HTTP stack and those that are not. Using both of these domains to ensure it works on both.
        clearCookiesForDomain("geocaching.com")
        clearCookiesForDomain(".geocaching.com")
        clearCookiesForDomain("https://geocaching.com")
        clearCookiesForDomain("https://.geocaching.com")

        CookieSyncManager.createInstance(this).sync()
    }

    companion object {
        @JvmStatic
        operator fun get(context: Context): App {
            return context.applicationContext as App
        }

        private fun clearCookiesForDomain(domain: String) {
            val cookieManager = CookieManager.getInstance()
            val cookies = cookieManager.getCookie(domain) ?: return

            for (cookie in cookies.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                val cookieParts = cookie.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (cookieParts.isNotEmpty()) {
                    val newCookie = cookieParts[0].trim { it <= ' ' } + "=;expires=Sat, 1 Jan 2000 00:00:01 UTC;"
                    cookieManager.setCookie(domain, newCookie)
                }
            }
        }
    }
}
