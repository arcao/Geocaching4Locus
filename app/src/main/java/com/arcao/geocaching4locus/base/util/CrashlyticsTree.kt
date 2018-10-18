package com.arcao.geocaching4locus.base.util

import android.util.Log
import com.crashlytics.android.Crashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.DebugTree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        Crashlytics.log(priority, tag, if (t == null) message else "$message\n${Log.getStackTraceString(t)}")
    }
}