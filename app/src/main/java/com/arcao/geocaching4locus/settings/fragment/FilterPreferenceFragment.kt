package com.arcao.geocaching4locus.settings.fragment

import android.content.SharedPreferences
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_CACHE_TYPE
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_CACHE_TYPE_PREFIX
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_CONTAINER_TYPE
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_CONTAINER_TYPE_PREFIX
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DIFFICULTY
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DIFFICULTY_MAX
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DIFFICULTY_MIN
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DISTANCE
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_TERRAIN
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_TERRAIN_MAX
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_TERRAIN_MIN
import com.arcao.geocaching4locus.base.constants.PrefConstants.IMPERIAL_UNITS
import com.arcao.geocaching4locus.base.constants.PrefConstants.SHORT_CACHE_TYPE_NAMES
import com.arcao.geocaching4locus.base.constants.PrefConstants.SHORT_CONTAINER_TYPE_NAMES
import com.arcao.geocaching4locus.base.constants.PrefConstants.UNIT_KM
import com.arcao.geocaching4locus.base.constants.PrefConstants.UNIT_MILES
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.data.account.AccountManager
import org.koin.android.ext.android.get

class FilterPreferenceFragment : AbstractPreferenceFragment() {
    private val premiumMember: Boolean by lazy {
        get<AccountManager>().isPremium
    }

    private val imperialUnits: Boolean by lazy {
        preferences.getBoolean(IMPERIAL_UNITS, false)
    }

    override val preferenceResource: Int
        get() = R.xml.preference_category_filter

    override fun preparePreference() {
        super.preparePreference()

        prepareCacheTypePreference()
        prepareContainerTypePreference()
        prepareDifficultyPreference()
        prepareTerrainPreference()
        prepareDistancePreference()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            FILTER_DISTANCE -> {
                preference<EditTextPreference>(key).apply {
                    summary = if (imperialUnits) {
                        preparePreferenceSummary(text + UNIT_MILES, R.string.pref_distance_summary_miles)
                    } else {
                        preparePreferenceSummary(text + UNIT_KM, R.string.pref_distance_summary_km)
                    }
                }
            }
        }
    }

    private fun prepareCacheTypePreference() {
        preference<Preference>(FILTER_CACHE_TYPE).apply {
            isEnabled = premiumMember
            summary = if (premiumMember) prepareCacheTypeSummary() else prepareCacheTypeSummaryBasicMember()
            if (!premiumMember) {
                applyPremiumTitleSign(this)
            }
        }
    }

    private fun prepareContainerTypePreference() {
        preference<Preference>(FILTER_CONTAINER_TYPE).apply {
            isEnabled = premiumMember
            summary = if (premiumMember) prepareContainerTypeSummary() else prepareContainerTypeSummaryBasicMember()
            if (!premiumMember) {
                applyPremiumTitleSign(this)
            }
        }
    }

    private fun prepareDifficultyPreference() {
        preference<Preference>(FILTER_DIFFICULTY).apply {
            isEnabled = premiumMember

            var difficultyMin = "1"
            var difficultyMax = "5"

            if (premiumMember) {
                difficultyMin = preferences.getString(FILTER_DIFFICULTY_MIN, difficultyMin)!!
                difficultyMax = preferences.getString(FILTER_DIFFICULTY_MAX, difficultyMax)!!
            } else {
                applyPremiumTitleSign(this)
            }

            summary = prepareRatingSummary(difficultyMin, difficultyMax)
        }
    }

    private fun prepareTerrainPreference() {
        preference<Preference>(FILTER_TERRAIN).apply {
            isEnabled = premiumMember

            var terrainMin = "1"
            var terrainMax = "5"

            if (premiumMember) {
                terrainMin = preferences.getString(FILTER_TERRAIN_MIN, terrainMin)!!
                terrainMax = preferences.getString(FILTER_TERRAIN_MAX, terrainMax)!!
            } else {
                applyPremiumTitleSign(this)
            }

            summary = prepareRatingSummary(terrainMin, terrainMax)
        }
    }

    private fun prepareDistancePreference() {
        preference<EditTextPreference>(FILTER_DISTANCE).apply {
            // set summary text
            summary = if (!imperialUnits) {
                preparePreferenceSummary(text + UNIT_KM, R.string.pref_distance_summary_km)
            } else {
                setDialogMessage(R.string.pref_distance_summary_miles)
                preparePreferenceSummary(text + UNIT_MILES, R.string.pref_distance_summary_miles)
            }
        }
    }

    private fun prepareRatingSummary(min: CharSequence, max: CharSequence): CharSequence {
        return preparePreferenceSummary("$min - $max", 0)
    }

    private fun prepareCacheTypeSummary(): CharSequence {
        val sb = StringBuilder()

        var allChecked = true
        var noneChecked = true

        val len = AppConstants.GEOCACHE_TYPES.size
        for (i in 0 until len) {
            if (preferences.getBoolean(FILTER_CACHE_TYPE_PREFIX + i, true)) {
                noneChecked = false
            } else {
                allChecked = false
            }
        }

        if (allChecked || noneChecked) {
            sb.append(getString(R.string.pref_geocache_type_all))
        } else {
            for (i in 0 until len) {
                if (preferences.getBoolean(FILTER_CACHE_TYPE_PREFIX + i, true)) {
                    if (sb.isNotEmpty())
                        sb.append(TEXT_VALUE_SEPARATOR)
                    sb.append(SHORT_CACHE_TYPE_NAMES[i])
                }
            }
        }

        return preparePreferenceSummary(sb.toString(), 0)
    }

    private fun prepareCacheTypeSummaryBasicMember(): CharSequence {
        return preparePreferenceSummary(
            SHORT_CACHE_TYPE_NAMES[0] +
                TEXT_VALUE_SEPARATOR +
                SHORT_CACHE_TYPE_NAMES[8] +
                TEXT_VALUE_SEPARATOR +
                SHORT_CACHE_TYPE_NAMES[10],
            0
        )
    }

    private fun prepareContainerTypeSummary(): CharSequence {
        val sb = StringBuilder()

        val len = AppConstants.GEOCACHE_SIZES.size
        for (i in 0 until len) {
            if (preferences.getBoolean(FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
                if (sb.isNotEmpty())
                    sb.append(TEXT_VALUE_SEPARATOR)
                sb.append(SHORT_CONTAINER_TYPE_NAMES[i])
            }
        }

        if (sb.isEmpty()) {
            for (i in 0 until len) {
                if (sb.isNotEmpty())
                    sb.append(TEXT_VALUE_SEPARATOR)
                sb.append(SHORT_CONTAINER_TYPE_NAMES[i])
            }
        }

        return preparePreferenceSummary(sb.toString(), 0)
    }

    private fun prepareContainerTypeSummaryBasicMember(): CharSequence {
        val sb = StringBuilder()

        val len = AppConstants.GEOCACHE_SIZES.size
        for (i in 0 until len) {
            if (sb.isNotEmpty())
                sb.append(TEXT_VALUE_SEPARATOR)
            sb.append(SHORT_CONTAINER_TYPE_NAMES[i])
        }

        return preparePreferenceSummary(sb.toString(), 0)
    }

    companion object {
        private const val TEXT_VALUE_SEPARATOR = ", "
    }
}
