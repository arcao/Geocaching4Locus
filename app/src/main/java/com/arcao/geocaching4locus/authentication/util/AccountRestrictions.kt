package com.arcao.geocaching4locus.authentication.util

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.arcao.geocaching.api.data.GeocacheLimits
import com.arcao.geocaching.api.data.apilimits.ApiLimits
import com.arcao.geocaching.api.data.type.ContainerType
import com.arcao.geocaching.api.data.type.GeocacheType
import com.arcao.geocaching4locus.base.constants.PrefConstants
import java.util.Calendar
import java.util.Date

class AccountRestrictions internal constructor(context: Context) {

    private val context: Context = context.applicationContext
    private val preferences =
        this.context.getSharedPreferences(PrefConstants.RESTRICTION_STORAGE_NAME, Context.MODE_PRIVATE)

    var maxFullGeocacheLimit: Long = 0
        private set
    private var currentFullGeocacheLimit: Long = 0
    var fullGeocacheLimitPeriod: Long = 0
        private set
    private lateinit var renewFullGeocacheLimit: Date

    init {
        init()
    }

    fun remove() {
        preferences.edit()
            .remove(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT)
            .remove(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT)
            .remove(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD)
            .remove(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT)
            .apply()

        init()
    }

    private fun init() {
        maxFullGeocacheLimit = preferences.getLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, Long.MAX_VALUE)
        currentFullGeocacheLimit = preferences.getLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, 0)
        fullGeocacheLimitPeriod = preferences.getLong(
            PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD,
            DEFAULT_FULL_GEOCACHE_LIMIT_PERIOD
        )
        renewFullGeocacheLimit = Date(preferences.getLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, 0))
    }

    internal fun applyRestrictions(premium: Boolean) {
        if (premium) {
            presetPremiumMembershipConfiguration()
        } else {
            presetBasicMembershipConfiguration()
        }
    }

    private fun presetBasicMembershipConfiguration() {
        PreferenceManager.getDefaultSharedPreferences(context).edit {
            // DOWNLOADING
            putBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, true)
            putString(
                PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
                PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_NEVER
            )
            putInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 0)
            // LIVE MAP
            putBoolean(PrefConstants.LIVE_MAP_DOWNLOAD_HINTS, false)
            // FILTERS
            putString(PrefConstants.FILTER_DIFFICULTY_MIN, "1")
            putString(PrefConstants.FILTER_DIFFICULTY_MAX, "5")
            putString(PrefConstants.FILTER_TERRAIN_MIN, "1")
            putString(PrefConstants.FILTER_TERRAIN_MAX, "5")

            // multi-select filters (select all)
            val geocacheTypeLength = GeocacheType.values().size
            for (i in 0 until geocacheTypeLength)
                putBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)
            val containerTypeLength = ContainerType.values().size
            for (i in 0 until containerTypeLength)
                putBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)
        }
    }

    private fun presetPremiumMembershipConfiguration() {
        val defaultPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        defaultPreferences.edit {
            // DOWNLOADING
            putBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, false)
            putString(
                PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW,
                PrefConstants.DOWNLOADING_FULL_CACHE_DATE_ON_SHOW__UPDATE_ONCE
            )
            putInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5)
        }
    }

    fun updateLimits(apiLimits: ApiLimits?) {
        val limits = apiLimits?.cacheLimits() ?: return
        if (limits.isEmpty())
            return

        val limit = limits[0]

        maxFullGeocacheLimit = limit.limit()
        fullGeocacheLimitPeriod = limit.period()

        preferences.edit()
            .putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit)
            .putLong(PrefConstants.RESTRICTION__FULL_GEOCACHE_LIMIT_PERIOD, fullGeocacheLimitPeriod)
            .apply()
    }

    fun updateLimits(cacheLimits: GeocacheLimits?) {
        maxFullGeocacheLimit = cacheLimits?.maxGeocacheCount()?.toLong() ?: return

        preferences.edit {
            // cache limit was renew
            if (currentFullGeocacheLimit > cacheLimits.currentGeocacheCount() || currentFullGeocacheLimit == 0L && cacheLimits.currentGeocacheCount() > 0) {
                currentFullGeocacheLimit = cacheLimits.currentGeocacheCount().toLong()

                val c = Calendar.getInstance()
                c.add(Calendar.MINUTE, fullGeocacheLimitPeriod.toInt())

                renewFullGeocacheLimit = c.time

                // store it to preferences
                putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit)
                putLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit)
                putLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, renewFullGeocacheLimit.time)
            } else {
                currentFullGeocacheLimit = cacheLimits.currentGeocacheCount().toLong()

                // store it to preferences
                putLong(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit)
                putLong(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit)
            }
        }
    }

    fun getRenewFullGeocacheLimit(): Date? {
        checkRenewPeriod()

        return renewFullGeocacheLimit
    }

    private fun checkRenewPeriod() {
        if (renewFullGeocacheLimit.before(Date())) {
            val c = Calendar.getInstance()
            c.add(Calendar.MINUTE, fullGeocacheLimitPeriod.toInt())
            renewFullGeocacheLimit = c.time
            currentFullGeocacheLimit = 0
        }
    }

    companion object {
        private const val DEFAULT_FULL_GEOCACHE_LIMIT_PERIOD: Long = 1440 // 24 hours in minutes
    }
}
