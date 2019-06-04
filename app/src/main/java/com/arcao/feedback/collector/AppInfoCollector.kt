package com.arcao.feedback.collector

import com.arcao.geocaching4locus.App

class AppInfoCollector(private val app: App) : Collector() {
    override val name: String
        get() = "APP INFO"

    override suspend fun collect(): String {
        val sb = StringBuilder()

        sb.append("APP_PACKAGE=").append(app.packageName).append("\n")
        sb.append("APP_VERSION_CODE=").append(app.versionCode).append("\n")
        sb.append("APP_VERSION_NAME=").append(app.version).append("\n")

        return sb.toString()
    }
}
