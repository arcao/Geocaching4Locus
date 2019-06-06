package com.arcao.geocaching4locus.base.constants

import android.net.Uri
import android.os.Build
import android.util.Base64
import com.arcao.geocaching4locus.data.api.model.GeocacheSize
import com.arcao.geocaching4locus.data.api.model.GeocacheType

import locus.api.android.utils.LocusUtils

object AppConstants {
    const val OAUTH_CALLBACK_URL = "https://geocaching4locus.eu/oauth"

    val USERS_GUIDE_URI = Uri.parse("https://geocaching4locus.eu/users-guide/")!!
    val WEBSITE_URI = Uri.parse("https://geocaching4locus.eu/")!!
    val FACEBOOK_URI = Uri.parse("https://www.facebook.com/Geocaching4Locus")!!
    val GEOCACHING_LIVE_URI = Uri.parse("http://www.geocaching.com/live")!!
    val POWER_SAVE_INFO_URI = Uri.parse("https://geocaching4locus.eu/redirect/power-save-issue")!!

    // Saved in Base64 because Google Play doesn't allow donation via Paypal.
    // This will prevent it to autodetect by robot.
    // params: %s = currency referenceCode (ISO-4217)
    val DONATE_PAYPAL_URI = String(
        Base64.decode(
            "aHR0cHM6Ly93d3cucGF5cGFsLmNvbS9jZ2ktYmluL3dlYnNjcj9jbWQ9X2RvbmF0aW9ucyZidXNpbmVzcz1hcmNhbyUlNDBhcmNhbyUlMmVjb20mbGM9Q1omaXRlbV9uYW1lPUdlb2NhY2hpbmc0TG9jdXMmaXRlbV9udW1iZXI9ZzRsJmN1cnJlbmN5X2NvZGU9JXMmYm49UFAlJTJkRG9uYXRpb25zQkYlJTNhYnRuX2RvbmF0ZUNDX0xHJSUyZWdpZiUlM2FOb25Ib3N0ZWQ=",
            Base64.DEFAULT
        )
    )

    const val LOCUS_MIN_VERSION = "3.36.0"
    val LOCUS_MIN_VERSION_CODE: LocusUtils.VersionCode = LocusUtils.VersionCode.UPDATE_15

    /* Adaptive downloading configuration */
    const val ADAPTIVE_DOWNLOADING_MIN_ITEMS = 10
    const val ADAPTIVE_DOWNLOADING_MAX_ITEMS = 100
    const val ADAPTIVE_DOWNLOADING_STEP = 20
    const val ADAPTIVE_DOWNLOADING_MIN_TIME_MS = 3500 // more than time required for 30 calls per minute
    const val ADAPTIVE_DOWNLOADING_MAX_TIME_MS = 10000
    const val ITEMS_PER_REQUEST = 30

    const val MINUTES_PER_HOUR = 60

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
    const val DISTANCE_MIN_METERS = 100
    const val DISTANCE_MAX_METERS = 50000

    val PREMIUM_CHARACTER =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) String(Character.toChars(0x1F451)) else "(PM)"

    const val NOTIFICATION_ID_LIVEMAP = 1

    private const val LIVEMAP_REQUESTS = 5
    const val LIVEMAP_CACHES_PER_REQUEST = 50
    const val LIVEMAP_CACHES_COUNT = LIVEMAP_REQUESTS * LIVEMAP_CACHES_PER_REQUEST
    const val LIVEMAP_DISTANCE = 60000
    const val LIVEMAP_PACK_WAYPOINT_PREFIX = "LiveMap|"

    val GEOCACHE_TYPES = arrayOf(
        GeocacheType.TRADITIONAL,
        GeocacheType.MULTI_CACHE,
        GeocacheType.MYSTERY_UNKNOWN,
        GeocacheType.VIRTUAL,
        GeocacheType.EARTHCACHE,
        GeocacheType.PROJECT_APE,
        GeocacheType.LETTERBOX_HYBRID,
        GeocacheType.WHERIGO,
        GeocacheType.EVENT,
        GeocacheType.MEGA_EVENT,
        GeocacheType.CACHE_IN_TRASH_OUT_EVENT,
        GeocacheType.GPS_ADVENTURES_EXHIBIT,
        GeocacheType.WEBCAM,
        GeocacheType.LOCATIONLESS_CACHE,
        GeocacheType.LOST_AND_FOUND_EVENT_CACHE,
        GeocacheType.GEOCACHING_HQ,
        GeocacheType.GEOCACHING_LOST_AND_FOUND_CELEBRATION,
        GeocacheType.GEOCACHING_BLOCK_PARTY,
        GeocacheType.GIGA_EVENT
    )

    val GEOCACHE_SIZES = arrayOf(
        GeocacheSize.NOT_CHOSEN,
        GeocacheSize.MICRO,
        GeocacheSize.SMALL,
        GeocacheSize.MEDIUM,
        GeocacheSize.LARGE,
        GeocacheSize.VIRTUAL,
        GeocacheSize.OTHER
    )
}
