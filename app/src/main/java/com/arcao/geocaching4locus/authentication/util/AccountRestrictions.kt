package com.arcao.geocaching4locus.authentication.util

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.arcao.geocaching4locus.data.api.model.User
import com.arcao.geocaching4locus.data.api.model.enums.MembershipType
import java.time.Duration
import java.time.Instant

class AccountRestrictions internal constructor(context: Context) {

    private val context: Context = context.applicationContext
    private val preferences =
        this.context.getSharedPreferences(PrefConstants.RESTRICTION_STORAGE_NAME, Context.MODE_PRIVATE)

    var maxFullGeocacheLimit: Int = 0
        private set
    var maxLiteGeocacheLimit: Int = 0
        private set

    var currentFullGeocacheLimit: Int = 0
        private set
    var currentLiteGeocacheLimit: Int = 0
        private set

    var renewFullGeocacheLimit: Instant = Instant.now()
        get() {
            val now = Instant.now()
            if (field.isBefore(now)) {
                field = now
            }
            return field
        }
        private set

    var renewLiteGeocacheLimit: Instant = Instant.now()
        get() {
            val now = Instant.now()
            if (field.isBefore(now)) {
                field = now
            }
            return field
        }
        private set

    init {
        init()
    }

    fun remove() {
        preferences.edit {
            clear()
            putInt(PrefConstants.PREF_VERSION, PrefConstants.CURRENT_PREF_VERSION)
        }

        init()
    }

    private fun init() {
        val version = preferences.getInt(PrefConstants.PREF_VERSION, 0)
        if (version != PrefConstants.CURRENT_PREF_VERSION) {
            preferences.edit {
                clear()
                putInt(PrefConstants.PREF_VERSION, PrefConstants.CURRENT_PREF_VERSION)
            }
        }

        maxFullGeocacheLimit = preferences.getInt(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, 0)
        currentFullGeocacheLimit = preferences.getInt(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, 0)
        renewFullGeocacheLimit = Instant.ofEpochSecond(preferences.getLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, 0))

        maxLiteGeocacheLimit = preferences.getInt(PrefConstants.RESTRICTION__MAX_LITE_GEOCACHE_LIMIT, 0)
        currentLiteGeocacheLimit = preferences.getInt(PrefConstants.RESTRICTION__CURRENT_LITE_GEOCACHE_LIMIT, 0)
        renewLiteGeocacheLimit = Instant.ofEpochSecond(preferences.getLong(PrefConstants.RESTRICTION__RENEW_LITE_GEOCACHE_LIMIT, 0))
    }

    internal fun applyRestrictions(user: User) {
        if (user.isPremium()) {
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
            for (i in 0 until AppConstants.GEOCACHE_TYPES.size)
                putBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)
            for (i in 0 until AppConstants.GEOCACHE_SIZES.size)
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

    fun updateLimits(user: User) {
        maxFullGeocacheLimit = if (user.isPremium()) {
            FULL_GEOCACHE_LIMIT_PREMIUM
        } else {
            FULL_GEOCACHE_LIMIT_BASIC
        }

        maxLiteGeocacheLimit = if (user.isPremium()) {
            LITE_GEOCACHE_LIMIT_PREMIUM
        } else {
            LITE_GEOCACHE_LIMIT_BASIC
        }

        val limits = user.geocacheLimits ?: return

        preferences.edit {
            currentFullGeocacheLimit = limits.fullCallsRemaining
            renewFullGeocacheLimit = Instant.now().plus(limits.fullCallsSecondsToLive ?: DEFAULT_RENEW_DURATION)

            currentLiteGeocacheLimit = limits.liteCallsRemaining
            renewLiteGeocacheLimit = Instant.now().plus(limits.liteCallsSecondsToLive ?: DEFAULT_RENEW_DURATION)

            // store it to preferences
            putInt(PrefConstants.RESTRICTION__MAX_FULL_GEOCACHE_LIMIT, maxFullGeocacheLimit)
            putInt(PrefConstants.RESTRICTION__CURRENT_FULL_GEOCACHE_LIMIT, currentFullGeocacheLimit)
            putLong(PrefConstants.RESTRICTION__RENEW_FULL_GEOCACHE_LIMIT, renewFullGeocacheLimit.epochSecond)

            putInt(PrefConstants.RESTRICTION__MAX_LITE_GEOCACHE_LIMIT, maxLiteGeocacheLimit)
            putInt(PrefConstants.RESTRICTION__CURRENT_LITE_GEOCACHE_LIMIT, currentLiteGeocacheLimit)
            putLong(PrefConstants.RESTRICTION__RENEW_LITE_GEOCACHE_LIMIT, renewLiteGeocacheLimit.epochSecond)
        }
    }

    companion object {
        private const val FULL_GEOCACHE_LIMIT_PREMIUM = 16000
        private const val FULL_GEOCACHE_LIMIT_BASIC = 3
        private const val LITE_GEOCACHE_LIMIT_PREMIUM = 10000
        private const val LITE_GEOCACHE_LIMIT_BASIC = 10000

        val DEFAULT_RENEW_DURATION: Duration = Duration.ofDays(1)
    }

    private fun User.isPremium() = when (membership) {
        MembershipType.UNKNOWN -> false
        MembershipType.BASIC -> false
        MembershipType.CHARTER -> true
        MembershipType.PREMIUM -> true
    }
}
