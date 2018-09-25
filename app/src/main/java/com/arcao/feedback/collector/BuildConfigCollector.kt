package com.arcao.feedback.collector

import com.arcao.geocaching4locus.BuildConfig

class BuildConfigCollector : Collector() {
    override val name: String
        get() = "BuildConfig INFO"

    override fun collect(): String {
        return "APPLICATION_ID=${BuildConfig.APPLICATION_ID}\n" +
                "BUILD_TIME=${BuildConfig.BUILD_TIME}\n" +
                "BUILD_TYPE=${BuildConfig.BUILD_TYPE}\n" +
                "DEBUG=${BuildConfig.DEBUG}\n" +
                "FLAVOR=${BuildConfig.FLAVOR}\n" +
                "GEOCACHING_API_STAGING=${BuildConfig.GEOCACHING_API_STAGING}\n" +
                "GIT_SHA=${BuildConfig.GIT_SHA}\n" +
                "VERSION_CODE=${BuildConfig.VERSION_CODE}\n" +
                "VERSION_NAME=${BuildConfig.VERSION_NAME}\n"
    }
}
