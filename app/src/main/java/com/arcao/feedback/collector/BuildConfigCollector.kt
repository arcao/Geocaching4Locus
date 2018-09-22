package com.arcao.feedback.collector

import com.arcao.geocaching4locus.BuildConfig

class BuildConfigCollector : Collector() {
    override val name: String
        get() = "BuildConfig INFO"

    override fun collect(): String {
        return "APPLICATION_ID=" + BuildConfig.APPLICATION_ID +
                "\nBUILD_TIME=" + BuildConfig.BUILD_TIME +
                "\nBUILD_TYPE=" + BuildConfig.BUILD_TYPE +
                "\nDEBUG=" + BuildConfig.DEBUG +
                "\nFLAVOR=" + BuildConfig.FLAVOR +
                "\nGEOCACHING_API_STAGING=" + BuildConfig.GEOCACHING_API_STAGING +
                "\nGIT_SHA=" + BuildConfig.GIT_SHA +
                "\nVERSION_CODE=" + BuildConfig.VERSION_CODE +
                "\nVERSION_NAME=" + BuildConfig.VERSION_NAME +
                "\n"
    }
}
