@file:Suppress("DEPRECATION")

package com.arcao.geocaching4locus

import android.app.Application
import android.webkit.CookieManager
import androidx.annotation.WorkerThread
import androidx.core.content.edit
import androidx.core.content.pm.PackageInfoCompat
import androidx.preference.PreferenceManager
import com.arcao.feedback.feedbackModule
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.constants.CrashlyticsConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.arcao.geocaching4locus.base.util.AnalyticsManager
import com.arcao.geocaching4locus.base.util.CrashlyticsTree
import com.arcao.geocaching4locus.base.util.KoinTimberLogger
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.geocachingApiModule
import com.google.firebase.crashlytics.FirebaseCrashlytics
import locus.api.android.utils.LocusUtils
import locus.api.locusMapApiModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.UUID

class App : Application() {
    private val accountManager by inject<AccountManager>()
    private val analyticsManager by inject<AnalyticsManager>()

    private val deviceId: String by lazy {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        var value = pref.getString(PrefConstants.DEVICE_ID, null)

        if (value.isNullOrEmpty()) {
            value = UUID.randomUUID().toString()

            pref.edit {
                putString(PrefConstants.DEVICE_ID, value)
            }
        }
        value
    }

    val version: String by lazy {
        try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            Timber.e(e)
            "1.0"
        }
    }

    val versionCode: Long by lazy {
        try {
            PackageInfoCompat.getLongVersionCode(packageManager.getPackageInfo(packageName, 0))
        } catch (e: Exception) {
            Timber.e(e)
            0L
        }
    }

    val name: String by lazy {
        getString(applicationInfo.labelRes)
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)

            if (BuildConfig.DEBUG) {
                logger(KoinTimberLogger())
            }

            modules(listOf(appModule, geocachingApiModule, locusMapApiModule, feedbackModule))
        }

        prepareCrashlytics()

        analyticsManager.setPremiumMember(accountManager.isPremium)
    }

    private fun prepareCrashlytics() {
        // Set up Crashlytics, disabled for debug builds
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }

        val crashlytics = FirebaseCrashlytics.getInstance()
        Timber.plant(Timber.DebugTree())
        Timber.plant(CrashlyticsTree(crashlytics))

        crashlytics.setUserId(deviceId)
        crashlytics.setCustomKey(CrashlyticsConstants.PREMIUM_MEMBER, accountManager.isPremium)

        val lv = try {
            LocusUtils.getActiveVersion(this)
        } catch (t: Throwable) {
            Timber.e(t)
            null
        }

        crashlytics.setCustomKey(CrashlyticsConstants.LOCUS_VERSION, lv?.versionName.orEmpty())
        crashlytics.setCustomKey(CrashlyticsConstants.LOCUS_PACKAGE, lv?.packageName.orEmpty())
    }

    @WorkerThread
    fun clearGeocachingCookies() {
        // required to call
        flushCookie()

        // setCookie acts differently when trying to expire cookies between builds of Android that are using
        // Chromium HTTP stack and those that are not. Using both of these domains to ensure it works on both.
        clearCookiesForDomain("geocaching.com")
        clearCookiesForDomain(".geocaching.com")
        clearCookiesForDomain("https://geocaching.com")
        clearCookiesForDomain("https://.geocaching.com")

        flushCookie()
    }

    private fun flushCookie() {
        CookieManager.getInstance().flush()
    }

    companion object {
        private fun clearCookiesForDomain(domain: String) {
            val cookieManager = CookieManager.getInstance()
            val cookies = cookieManager.getCookie(domain) ?: return

            for (cookie in cookies.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()) {
                val cookieParts = cookie.split("=").dropLastWhile { it.isEmpty() }.toTypedArray()
                if (cookieParts.isNotEmpty()) {
                    val newCookie =
                        cookieParts[0].trim(Character::isWhitespace) + "=;expires=Sat, 1 Jan 2000 00:00:01 UTC;"
                    cookieManager.setCookie(domain, newCookie)
                }
            }
        }
    }
}

