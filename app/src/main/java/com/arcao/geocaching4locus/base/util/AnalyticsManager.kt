package com.arcao.geocaching4locus.base.util

import android.content.Context

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent

class AnalyticsManager(val context: Context) {
    private val firebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    fun actionLogin(success: Boolean, premiumMember: Boolean) {
        firebaseAnalytics.logEvent(EVENT_LOGIN) {
            param(PARAM_SUCCESS, success.toString())
            param(PARAM_PREMIUM_MEMBER, premiumMember.toString())
        }
    }

    fun actionUpdateAccount(success: Boolean, premiumMember: Boolean) {
        firebaseAnalytics.logEvent(EVENT_UPDATE_ACCOUNT) {
            param(PARAM_SUCCESS, success.toString())
            param(PARAM_PREMIUM_MEMBER, premiumMember.toString())
        }
    }

    fun actionDashboard(calledFromLocus: Boolean) {
        firebaseAnalytics.logEvent(EVENT_DASHBOARD) {
            param(PARAM_CALLED_FROM_LOCUS, calledFromLocus.toString())
        }
    }

    fun actionImport(premiumMember: Boolean) {
        firebaseAnalytics.logEvent(EVENT_IMPORT) {
            param(PARAM_PREMIUM_MEMBER, premiumMember.toString())
        }
    }

    fun actionImportBookmarks(count: Int, all: Boolean) {
        firebaseAnalytics.logEvent(EVENT_IMPORT_BOOKMARKS) {
            param(PARAM_COUNT, count.toLong())
            param(PARAM_ALL, all.toString())
        }
    }

    fun actionImportGC(premiumMember: Boolean) {
        firebaseAnalytics.logEvent(EVENT_IMPORT_GC) {
            param(PARAM_PREMIUM_MEMBER, premiumMember.toString())
        }
    }

    fun actionSearchNearest(
        coordinatesSource: String?,
        useFilter: Boolean,
        count: Int,
        premiumMember: Boolean
    ) {
        firebaseAnalytics.logEvent(EVENT_SEARCH_NEAREST) {
            param(PARAM_COORDINATES_SOURCE, (coordinatesSource ?: COORDINATES_SOURCE_MANUAL))
            param(PARAM_USE_FILTER, useFilter.toString())
            param(PARAM_COUNT, count.toLong())
            param(PARAM_PREMIUM_MEMBER, premiumMember.toString())
        }
    }

    fun actionUpdate(oldPoint: Boolean, updateLogs: Boolean, premiumMember: Boolean) {
        firebaseAnalytics.logEvent(EVENT_UPDATE) {
            param(PARAM_OLD_POINT, oldPoint.toString())
            param(PARAM_UPDATE_LOGS, updateLogs.toString())
            param(PARAM_PREMIUM_MEMBER, premiumMember.toString())
        }
    }

    fun actionUpdateMore(count: Int, premiumMember: Boolean) {
        firebaseAnalytics.logEvent(EVENT_UPDATE_MORE) {
            param(PARAM_COUNT, count.toLong())
            param(PARAM_PREMIUM_MEMBER, premiumMember.toString())
        }
    }

    fun setPremiumMember(premium: Boolean) {
        firebaseAnalytics.setUserProperty(PROP_PREMIUM, premium.toString())
    }

    companion object {
        const val COORDINATES_SOURCE_LOCUS = "LOCUS"
        const val COORDINATES_SOURCE_GPS = "GPS"
        const val COORDINATES_SOURCE_MANUAL = "MANUAL"

        private const val EVENT_LOGIN = FirebaseAnalytics.Event.LOGIN
        private const val EVENT_UPDATE_ACCOUNT = "Update_account"
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
