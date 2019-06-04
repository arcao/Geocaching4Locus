package com.arcao.geocaching4locus.base.constants

object PrefConstants {
    const val ACCOUNT_STORAGE_NAME = "ACCOUNT"
    const val RESTRICTION_STORAGE_NAME = "RESTRICTION"

    const val PREF_VERSION = "pref_version"
    const val CURRENT_PREF_VERSION = 4

    const val LAST_LATITUDE = "latitude"
    const val LAST_LONGITUDE = "longitude"

    const val ACCOUNT_USERNAME = "username"
    const val ACCOUNT_PREMIUM = "premium_account"
    const val ACCOUNT_AVATAR_URL = "avatar_url"
    const val ACCOUNT_HOME_COORDINATES_LAT = "home_coordinates_lat"
    const val ACCOUNT_HOME_COORDINATES_LON = "home_coordinates_lon"
    const val ACCOUNT_SESSION = "session"
    const val ACCOUNT_LAST_ACCOUNT_UPDATE_TIME = "last_account_update_time"

    @Deprecated("")
    const val ACCOUNT_PASSWORD = "password"

    const val DEVICE_ID = "device_id"

    const val OAUTH_TOKEN = "OAUTH_TOKEN"
    const val OAUTH_TOKEN_SECRET = "OAUTH_TOKEN_SECRET"
    const val OAUTH_CALLBACK_CONFIRMED = "OAUTH_CALLBACK_CONFIRMED"

    const val FILTER_CACHE_TYPE = "cache_type_filter"
    const val FILTER_CACHE_TYPE_PREFIX = "filter_"
    const val FILTER_CONTAINER_TYPE = "container_type_filter"
    const val FILTER_CONTAINER_TYPE_PREFIX = "container_filter_"
    const val FILTER_DIFFICULTY = "difficulty_filter"
    const val FILTER_DIFFICULTY_MIN = "difficulty_filter_min"
    const val FILTER_DIFFICULTY_MAX = "difficulty_filter_max"
    const val FILTER_TERRAIN = "terrain_filter"
    const val FILTER_TERRAIN_MIN = "terrain_filter_min"
    const val FILTER_TERRAIN_MAX = "terrain_filter_max"
    const val FILTER_DISTANCE = "filter_distance"
    const val FILTER_SHOW_FOUND = "filter_show_found"
    const val FILTER_SHOW_OWN = "filter_show_own"
    const val FILTER_SHOW_DISABLED = "filter_show_disabled"

    const val LIVE_MAP = "live_map"
    const val SHOW_LIVE_MAP_DISABLED_NOTIFICATION = "show_live_map_disabled_notification"
    const val LIVE_MAP_DOWNLOAD_HINTS = "live_map_download_hints"
    const val LIVE_MAP_HIDE_CACHES_ON_DISABLED = "live_map_hide_caches_on_disabled"
    const val LIVE_MAP_LAST_REQUESTS = "live_map_last_requests"

    const val DOWNLOADING_SIMPLE_CACHE_DATA = "simple_cache_data"
    const val DOWNLOADING_FULL_CACHE_DATE_ON_SHOW = "full_cache_data_on_show"
    const val DOWNLOADING_COUNT_OF_CACHES = "filter_count_of_caches"
    const val DOWNLOADING_COUNT_OF_LOGS = "downloading_count_of_logs"
    const val DOWNLOADING_COUNT_OF_CACHES_STEP = "downloading_count_of_caches_step"
    const val DOWNLOAD_LOGS_UPDATE_CACHE = "download_logs_update_cache"
    const val DOWNLOADING_DISABLE_DNF_NM_NA_CACHES = "disable_dnf_nm_na_caches"
    const val DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT = "disable_dnf_nm_na_caches_logs_count"

    const val IMPERIAL_UNITS = "imperial_units"

    const val ABOUT_VERSION = "about_version"
    const val ABOUT_WEBSITE = "about_website"
    const val ABOUT_FACEBOOK = "about_facebook"
    const val ABOUT_FEEDBACK = "about_feedback"
    const val ABOUT_DONATE_PAYPAL = "about_donate_paypal"
    const val ACCOUNT_GEOCACHING_LIVE = "account_geocaching_live"

    const val RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT = "renew_full_geocache_limit"
    const val RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT = "current_full_geocache_limit"
    const val RESTRICTION__MAX_FULL_GEOCACHE_LIMIT = "max_full_geocache_limit"

    const val RESTRICTION__RENEW_LITE_GEOCACHE_LIMIT = "renew_lite_geocache_limit"
    const val RESTRICTION__CURRENT_LITE_GEOCACHE_LIMIT = "current_lite_geocache_limit"
    const val RESTRICTION__MAX_LITE_GEOCACHE_LIMIT = "max_lite_geocache_limit"

    const val DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE = "0"
    const val DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_EVERY = "1"
    const val DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER = "2"

    const val HIDE_POWER_MANAGEMENT_WARNING = "hide_power_management_warning"

    const val UNIT_KM = "km"
    const val UNIT_MILES = "mi"

    val SHORT_CACHE_TYPE_NAMES = arrayOf(
        "Tradi",
        "Multi",
        "Mystery",
        "Virtual",
        "Earth",
        "APE",
        "Letter",
        "Wherigo",
        "Event",
        "M-Event",
        "CITO",
        "Advent",
        "Webcam",
        "Loc-less",
        "L&F",
        "GS HQ",
        "GS L&F",
        "GS Party",
        "G-Event"
    )

    val SHORT_CONTAINER_TYPE_NAMES = arrayOf("?", "M", "S", "R", "L", "H", "O")
}