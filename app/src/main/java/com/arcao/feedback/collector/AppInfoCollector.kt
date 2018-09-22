package com.arcao.feedback.collector

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat

import timber.log.Timber

class AppInfoCollector(context: Context) : Collector() {
    private val context: Context = context.applicationContext

    override val name: String
        get() = "APP INFO"

    // To catch RuntimeException("Package manager has died") that can occur on some version of Android,
    // when the remote PackageManager is unavailable. I suspect this sometimes occurs when the App is being reinstalled.
    private val packageInfo: PackageInfo?
        get() {
            val pm = context.packageManager ?: return null

            return try {
                pm.getPackageInfo(context.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.v("Failed to find PackageInfo for current App : %s", context.packageName)
                null
            } catch (e: RuntimeException) {
                null
            }
        }

    override fun collect(): String {
        val sb = StringBuilder()

        packageInfo?.apply {
            sb.append("APP_PACKAGE=").append(packageName).append("\n")
            sb.append("APP_VERSION_CODE=").append(PackageInfoCompat.getLongVersionCode(this)).append("\n")
            sb.append("APP_VERSION_NAME=").append(versionName).append("\n")
        }

        return sb.toString()
    }
}
