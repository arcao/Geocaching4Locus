package com.arcao.geocaching4locus.base.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree(private val crashlytics: FirebaseCrashlytics) : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        crashlytics.log(
            priorityToString(priority) + if (t == null) {
                message
            } else {
                "$message\n${Log.getStackTraceString(t)}"
            }
        )
    }

    private fun priorityToString(priority: Int): String = when (priority) {
        Log.ASSERT -> "[A] "
        Log.DEBUG -> "[D] "
        Log.ERROR -> "[E] "
        Log.INFO -> "[I] "
        Log.VERBOSE -> "[V] "
        Log.WARN -> "[W] "
        else -> "[I] "
    }
}
