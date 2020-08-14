package com.arcao.geocaching4locus.base.util

import android.content.Context
import androidx.core.os.bundleOf

import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsManager(val context: Context) {
    private val firebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    fun actionLogin(success: Boolean, premiumMember: Boolean) {
        firebaseAnalytics.logEvent(
            EVENT_LOGIN,
            bundleOf(
                PARAM_SUCCESS to success.toString(),
                PARAM_PREMIUM_MEMBER to premiumMember.toString()
            )
        )
    }

    fun actionDashboard(calledFromLocus: Boolean) {
        firebaseAnalytics.logEvent(
            EVENT_DASHBOARD,
            bundleOf(
                PARAM_CALLED_FROM_LOCUS to calledFromLocus.toString()
            )
        )
    }

    fun actionImport(premiumMember: Boolean) {
        firebaseAnalytics.logEvent(
            EVENT_IMPORT,
            bundleOf(
                PARAM_PREMIUM_MEMBER to premiumMember.toString()
            )
        )
    }

    fun actionImportBookmarks(count: Int, all: Boolean) {
        firebaseAnalytics.logEvent(
            EVENT_IMPORT_BOOKMARKS,
            bundleOf(
                PARAM_COUNT to count,
                PARAM_ALL to all.toString()
            )
        )
    }

    fun actionImportGC(premiumMember: Boolean) {
        firebaseAnalytics.logEvent(
            EVENT_IMPORT_GC,
            bundleOf(
                PARAM_PREMIUM_MEMBER to premiumMember.toString()
            )
        )
    }

    fun actionSearchNearest(
        coordinatesSource: String?,
        useFilter: Boolean,
        count: Int,
        premiumMember: Boolean
    ) {
        firebaseAnalytics.logEvent(
            EVENT_SEARCH_NEAREST,
            bundleOf(
                PARAM_COORDINATES_SOURCE to (coordinatesSource ?: COORDINATES_SOURCE_MANUAL),
                PARAM_USE_FILTER to useFilter.toString(),
                PARAM_COUNT to count,
                PARAM_PREMIUM_MEMBER to premiumMember.toString()
            )
        )
    }

    fun actionUpdate(oldPoint: Boolean, updateLogs: Boolean, premiumMember: Boolean) {
        firebaseAnalytics.logEvent(
            EVENT_UPDATE,
            bundleOf(
                PARAM_OLD_POINT to oldPoint.toString(),
                PARAM_UPDATE_LOGS to updateLogs.toString(),
                PARAM_PREMIUM_MEMBER to premiumMember.toString()
            )
        )
    }

    fun actionUpdateMore(count: Int, premiumMember: Boolean) {
        firebaseAnalytics.logEvent(
            EVENT_UPDATE_MORE,
            bundleOf(
                PARAM_COUNT to count,
                PARAM_PREMIUM_MEMBER to premiumMember.toString()
            )
        )
    }

    fun setPremiumMember(premium: Boolean) {
        firebaseAnalytics.setUserProperty(PROP_PREMIUM, premium.toString())
    }

    companion object {
        const val COORDINATES_SOURCE_LOCUS = "LOCUS"
        const val COORDINATES_SOURCE_GPS = "GPS"
        const val COORDINATES_SOURCE_MANUAL = "MANUAL"

        private const val EVENT_LOGIN = FirebaseAnalytics.Event.LOGIN
        private const val EVENT_DASHBOARD = "Dashboard"
        private const val EVENT_IMPORT = "Import"
        private const val EVENT_IMPORT_BOOKMARKS = "Import_bookmarks"
        private const val EVENT_IMPORT_GC = "Import_GC"
        private const val EVENT_SEARCH_NEAREST = "Search_nearest"
        private const val EVENT_UPDATE = "Update"
        private const val EVENT_UPDATE_MORE = "Update_More"

        private const val PARAM_SUCCESS = "success"
        private const val PARAM_PREMIUM_MEMBER = "premium_member"
        private const val PARAM_CALLED_FROM_LOCUS = "called_from_locus"
        private const val PARAM_COUNT = "count"
        private const val PARAM_ALL = "all"
        private const val PARAM_COORDINATES_SOURCE = "coordinates_source"
        private const val PARAM_USE_FILTER = "use_filter"
        private const val PARAM_OLD_POINT = "old_point"
        private const val PARAM_UPDATE_LOGS = "update_logs"

        private const val PROP_PREMIUM = "premium"
    }
}
