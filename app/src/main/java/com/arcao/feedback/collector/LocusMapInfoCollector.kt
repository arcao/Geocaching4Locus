package com.arcao.feedback.collector

import android.content.Context
import locus.api.android.ActionTools
import locus.api.android.utils.LocusUtils

class LocusMapInfoCollector(private val context: Context) : Collector() {
    override val name: String
        get() = "LocusMapInfo"

    override suspend fun collect(): String {
        val sb = StringBuilder()

        try {
            val lv = LocusUtils.getActiveVersion(context)
            if (lv != null) {
                sb.append("Locus Version = ").append(lv.versionName)
                sb.append("\nLocus Package = ").append(lv.packageName)

                val info = ActionTools.getLocusInfo(context, lv)
                if (info != null) {
                    sb.append("\nLocus info:\n").append(info.toString())
                }
            } else {
                sb.append("Locus not installed!")
            }
        } catch (e: Exception) {
            sb.append("Unable to get info from Locus Map:\n").append(throwableToString(e))
        }

        return sb.toString()
    }
}
