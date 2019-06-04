package com.arcao.geocaching4locus.settings.manager

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.arcao.geocaching4locus.base.util.getParsedFloat
import com.arcao.geocaching4locus.base.util.getParsedInt
import kotlin.math.max
import kotlin.math.min

class DefaultPreferenceManager(
    context: Context
) {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val showLiveMapDisabledNotification
        get() = preferences.getBoolean(PrefConstants.SHOW_LIVE_MAP_DISABLED_NOTIFICATION, false)

    val hideGeocachesOnLiveMapDisabled
        get() = preferences.getBoolean(PrefConstants.LIVE_MAP_HIDE_CACHES_ON_DISABLED, false)

    val disableDnfNmNaGeocaches
        get() = preferences.getBoolean(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES, false)

    val disableDnfNmNaGeocachesThreshold
        get() = preferences.getInt(PrefConstants.DOWNLOADING_DISABLE_DNF_NM_NA_CACHES_LOGS_COUNT, 1)

    var liveMapLastRequests
        get() = preferences.getInt(PrefConstants.LIVE_MAP_LAST_REQUESTS, 0)
        set(value) = preferences.edit { putInt(PrefConstants.LIVE_MAP_LAST_REQUESTS, value) }

    val downloadingGeocacheLogsCount
        get() = preferences.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5)

    val downloadLogsUpdateCache
        get() = preferences.getBoolean(PrefConstants.DOWNLOAD_LOGS_UPDATE_CACHE, true)

    val downloadFullGeocacheOnShow
        get() = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE == preferences.getString(
            PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
            PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE
        )

    val downloadGeocacheOnShow
        get() = PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER != preferences.getString(
            PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
            PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE
        )

    val downloadDistanceMeters: Int
        get() {
            val imperialUnits = useImperialUnits
            val defaultValue = if (imperialUnits) {
                AppConstants.DISTANCE_MILES_DEFAULT
            } else {
                AppConstants.DISTANCE_KM_DEFAULT
            }

            var distance = preferences.getParsedFloat(PrefConstants.FILTER_DISTANCE, defaultValue)

            if (imperialUnits) {
                // to metric units [km]
                distance *= AppConstants.MILES_PER_KILOMETER
            }

            // to meters [m]
            distance *= 1000

            // fix for min and max distance error in Geocaching Live API
            return max(min(distance.toInt(), AppConstants.DISTANCE_MAX_METERS), AppConstants.DISTANCE_MIN_METERS)
        }

    var lastLatitude: Double
        get() = preferences.getFloat(PrefConstants.LAST_LATITUDE, Float.NaN).toDouble()
        set(value) = preferences.edit { putFloat(PrefConstants.LAST_LATITUDE, value.toFloat()) }

    var lastLongitude: Double
        get() = preferences.getFloat(PrefConstants.LAST_LONGITUDE, Float.NaN).toDouble()
        set(value) = preferences.edit { putFloat(PrefConstants.LAST_LONGITUDE, value.toFloat()) }

    var downloadingGeocachesCount: Int
        get() {
            var value = preferences.getInt(
                PrefConstants.DOWNLOADING_COUNT_OF_CACHES,
                AppConstants.DOWNLOADING_COUNT_OF_CACHES_DEFAULT
            )

            val step = downloadingGeocachesCountStep

            if (value > MAX_GEOCACHES_COUNT) {
                value = MAX_GEOCACHES_COUNT
                preferences.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, value).apply()
            }

            if (value % step != 0) {
                value = (value / step + 1) * step
                preferences.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, value).apply()
            }

            return value
        }
        set(value) {
            val step = downloadingGeocachesCountStep

            val correctValue = when {
                value > MAX_GEOCACHES_COUNT -> MAX_GEOCACHES_COUNT
                value % step != 0 -> (value / step + 1) * step
                else -> value
            }
            preferences.edit().putInt(PrefConstants.DOWNLOADING_COUNT_OF_CACHES, correctValue).apply()
        }

    val downloadingGeocachesCountStep: Int
        get() = preferences.getParsedInt(
            PrefConstants.DOWNLOADING_COUNT_OF_CACHES_STEP,
            AppConstants.DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT
        )

    private val useImperialUnits: Boolean
        get() = preferences.getBoolean(PrefConstants.IMPERIAL_UNITS, false)

    companion object {
        val MAX_GEOCACHES_COUNT: Int
            get() = if (Runtime.getRuntime().maxMemory() <= AppConstants.LOW_MEMORY_THRESHOLD) {
                AppConstants.DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY
            } else {
                AppConstants.DOWNLOADING_COUNT_OF_CACHES_MAX
            }
    }
}
