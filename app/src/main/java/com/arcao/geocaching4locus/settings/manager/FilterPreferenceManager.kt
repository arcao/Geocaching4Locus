package com.arcao.geocaching4locus.settings.manager

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.arcao.geocaching.api.data.type.ContainerType
import com.arcao.geocaching.api.data.type.GeocacheType
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.arcao.geocaching4locus.base.util.getParsedFloat

class FilterPreferenceManager(
    context: Context,
    private val accountManager: AccountManager
) {
    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val geocacheLogsCount
        get() = if (accountManager.isPremium) {
            preferences.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5)
        } else {
            0
        }

    val trackableLogsCount: Int = 0

    // TODO move to DefaultPreferenceManager
    val simpleCacheData
        get() = if (accountManager.isPremium) {
            preferences.getBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, false)
        } else {
            true
        }

    val showDisabled
        get() = preferences.getBoolean(PrefConstants.FILTER_SHOW_DISABLED, false)

    val showFound
        get() = preferences.getBoolean(PrefConstants.FILTER_SHOW_FOUND, false)

    val showOwn
        get() = preferences.getBoolean(PrefConstants.FILTER_SHOW_OWN, false)

    val difficultyMin
        get() = preferences.getParsedFloat(PrefConstants.FILTER_DIFFICULTY_MIN, 1f)

    val difficultyMax
        get() = preferences.getParsedFloat(PrefConstants.FILTER_DIFFICULTY_MAX, 5f)

    val terrainMin
        get() = preferences.getParsedFloat(PrefConstants.FILTER_TERRAIN_MIN, 1f)

    val terrainMax
        get() = preferences.getParsedFloat(PrefConstants.FILTER_TERRAIN_MAX, 5f)

    val excludeIgnoreList = true

    val geocacheTypes: Array<GeocacheType>
        get () {
            val len = GeocacheType.values().size
            val filter = ArrayList<GeocacheType>(len)

            for (i in 0 until len) {
                if (preferences.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
                    filter.add(GeocacheType.values()[i])
                }
            }

            return filter.toTypedArray()
        }
    val containerTypes: Array<ContainerType>
        get() {
            val len = ContainerType.values().size
            val filter = ArrayList<ContainerType>(len)

            for (i in 0 until len) {
                if (preferences.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
                    filter.add(ContainerType.values()[i])
                }
            }

            return filter.toTypedArray()
        }
}