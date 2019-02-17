package com.arcao.geocaching4locus.base.constants

import android.net.Uri
import android.os.Build
import android.util.Base64

import locus.api.android.utils.LocusUtils

object AppConstants {
    const val OAUTH_CALLBACK_URL = "https://geocaching4locus.eu/oauth"

    @JvmField
    val USERS_GUIDE_URI = Uri.parse("https://geocaching4locus.eu/users-guide/")!!
    @JvmField
    val WEBSITE_URI = Uri.parse("https://geocaching4locus.eu/")!!
    @JvmField
    val FACEBOOK_URI = Uri.parse("https://www.facebook.com/Geocaching4Locus")!!
    @JvmField
    val GPLUS_URI = Uri.parse("https://plus.google.com/+Geocaching4locusEu")!!
    @JvmField
    val GEOCACHING_LIVE_URI = Uri.parse("http://www.geocaching.com/live")!!
    @JvmField
    val POWER_SAVE_INFO_URI = Uri.parse("https://geocaching4locus.eu/redirect/power-save-issue")!!

    // Saved in Base64 because Google Play doesn't allow donation via Paypal.
    // This will prevent it to autodetect by robot.
    // params: %s = currency code (ISO-4217)
    @JvmField
    val DONATE_PAYPAL_URI = String(Base64.decode(
            "aHR0cHM6Ly93d3cucGF5cGFsLmNvbS9jZ2ktYmluL3dlYnNjcj9jbWQ9X2RvbmF0aW9ucyZidXNpbmVzcz1hcmNhbyUlNDBhcmNhbyUlMmVjb20mbGM9Q1omaXRlbV9uYW1lPUdlb2NhY2hpbmc0TG9jdXMmaXRlbV9udW1iZXI9ZzRsJmN1cnJlbmN5X2NvZGU9JXMmYm49UFAlJTJkRG9uYXRpb25zQkYlJTNhYnRuX2RvbmF0ZUNDX0xHJSUyZWdpZiUlM2FOb25Ib3N0ZWQ=",
            Base64.DEFAULT))

    const val LOCUS_MIN_VERSION = "3.8.0"
    val LOCUS_MIN_VERSION_CODE: LocusUtils.VersionCode = LocusUtils.VersionCode.UPDATE_09

    /* Adaptive downloading configuration */
    const val ADAPTIVE_DOWNLOADING_MIN_ITEMS = 5
    const val ADAPTIVE_DOWNLOADING_MAX_ITEMS = 50
    const val ADAPTIVE_DOWNLOADING_STEP = 5
    const val ADAPTIVE_DOWNLOADING_MIN_TIME_MS = 3500 // more than time required for 30 calls per minute
    const val ADAPTIVE_DOWNLOADING_MAX_TIME_MS = 10000
    const val ITEMS_PER_REQUEST = ADAPTIVE_DOWNLOADING_MIN_ITEMS

    const val SECONDS_PER_MINUTE = 60

    /* Search nearest cache count configuration */
    const val DOWNLOADING_COUNT_OF_CACHES_DEFAULT = 20
    const val DOWNLOADING_COUNT_OF_CACHES_MAX = 500
    const val DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY = 200
    const val DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT = 10

    const val LOW_MEMORY_THRESHOLD: Long = 16777216

    const val UPDATE_WITH_LOGS_COMPONENT = "com.arcao.geocaching4locus.UpdateWithLogsActivity"
    const val LOGS_PER_REQUEST = 30
    const val LOGS_TO_UPDATE_MAX = 100

    const val TRACKEBLES_PER_REQUEST = 30
    const val TRACKABLES_MAX = 60


    const val MILES_PER_KILOMETER = 1.609344f
    const val DISTANCE_KM_DEFAULT = 50f
    const val DISTANCE_MILES_DEFAULT = 50 / MILES_PER_KILOMETER

    // restrictions in Geocaching Live Service
    const val DISTANCE_KM_MIN = 0.1f
    const val DISTANCE_KM_MAX = 50f

    @JvmField
    val PREMIUM_CHARACTER = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) String(Character.toChars(0x1F451)) else "(PM)"

    const val NOTIFICATION_ID_LIVEMAP = 1

    const val LIVEMAP_REQUESTS = 5
    const val LIVEMAP_CACHES_PER_REQUEST = 50
    const val LIVEMAP_CACHES_COUNT = LIVEMAP_REQUESTS * LIVEMAP_CACHES_PER_REQUEST
    const val LIVEMAP_DISTANCE = 60000
    const val LIVEMAP_PACK_WAYPOINT_PREFIX = "LiveMap|"
}
