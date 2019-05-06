package com.arcao.geocaching4locus.base.util

import android.content.Context

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.crashlytics.android.answers.LoginEvent
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsUtil {
    const val COORDINATES_SOURCE_LOCUS = "LOCUS"
    const val COORDINATES_SOURCE_GPS = "GPS"
    const val COORDINATES_SOURCE_MANUAL = "MANUAL"

    @JvmStatic
    fun actionLogin(success: Boolean, premiumMember: Boolean) {
        Answers.getInstance().logLogin(
            LoginEvent().putSuccess(success)
                .putCustomAttribute("premium member", java.lang.Boolean.toString(premiumMember))
        )
    }

    @JvmStatic
    fun actionDashboard(calledFromLocus: Boolean) {
        Answers.getInstance().logCustom(
            CustomEvent("Dashboard")
                .putCustomAttribute("called from locus", java.lang.Boolean.toString(calledFromLocus))
        )
    }

    @JvmStatic
    fun actionImport(premiumMember: Boolean) {
        Answers.getInstance().logCustom(
            CustomEvent("Import")
                .putCustomAttribute("premium member", java.lang.Boolean.toString(premiumMember))
        )
    }

    @JvmStatic
    fun actionImportBookmarks(count: Int, all: Boolean) {
        Answers.getInstance().logCustom(
            CustomEvent("Import bookmarks")
                .putCustomAttribute("count", count).putCustomAttribute("all", java.lang.Boolean.toString(all))
        )
    }

    @JvmStatic
    fun actionImportGC(premiumMember: Boolean) {
        Answers.getInstance().logCustom(
            CustomEvent("Import GC")
                .putCustomAttribute("premium member", java.lang.Boolean.toString(premiumMember))
        )
    }

    @JvmStatic
    fun actionSearchNearest(coordinatesSource: String?, useFilter: Boolean, count: Int, premiumMember: Boolean) {
        Answers.getInstance().logCustom(
            CustomEvent("Search nearest")
                .putCustomAttribute(
                    "coordinates source", coordinatesSource
                        ?: COORDINATES_SOURCE_MANUAL
                )
                .putCustomAttribute("use filter", java.lang.Boolean.toString(useFilter))
                .putCustomAttribute("count", count)
                .putCustomAttribute("premium member", java.lang.Boolean.toString(premiumMember))
        )
    }

    @JvmStatic
    fun actionUpdate(oldPoint: Boolean, updateLogs: Boolean, premiumMember: Boolean) {
        Answers.getInstance().logCustom(
            CustomEvent("Update")
                .putCustomAttribute("old point", java.lang.Boolean.toString(oldPoint))
                .putCustomAttribute("update logs", java.lang.Boolean.toString(updateLogs))
                .putCustomAttribute("premium member", java.lang.Boolean.toString(premiumMember))
        )
    }

    @JvmStatic
    fun actionUpdateMore(count: Int, premiumMember: Boolean) {
        Answers.getInstance().logCustom(
            CustomEvent("Update More")
                .putCustomAttribute("count", count)
                .putCustomAttribute("premium member", java.lang.Boolean.toString(premiumMember))
        )
    }

    @JvmStatic
    fun setPremiumUser(app: Context, premium: Boolean) {
        FirebaseAnalytics.getInstance(app).setUserProperty("premium", java.lang.Boolean.toString(premium))
    }
}
