package com.arcao.geocaching4locus.settings.fragment.filter

import android.content.SharedPreferences
import androidx.preference.ListPreference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_TERRAIN_MAX
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_TERRAIN_MIN
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment

class TerrainFilterPreferenceFragment : AbstractPreferenceFragment() {
    override val preferenceResource: Int
        get() = R.xml.preference_category_filter_terrain

    override fun preparePreference() {
        super.preparePreference()

        val terrainMinPreference = preference<ListPreference>(FILTER_TERRAIN_MIN)
        val terrainMaxPreference = preference<ListPreference>(FILTER_TERRAIN_MAX)

        terrainMinPreference.summary = prepareRatingSummary(terrainMinPreference.value)
        terrainMinPreference.setOnPreferenceChangeListener { _, newValue ->
            val min = (newValue as String).toFloat()
            val max = terrainMaxPreference.value.toFloat()

            if (min > max) {
                terrainMaxPreference.value = newValue
                terrainMaxPreference.summary = prepareRatingSummary(newValue as CharSequence)
            }
            true
        }

        terrainMaxPreference.summary = prepareRatingSummary(terrainMaxPreference.value)
        terrainMaxPreference.setOnPreferenceChangeListener { _, newValue ->
            val min = terrainMinPreference.value.toFloat()
            val max = (newValue as String).toFloat()

            if (min > max) {
                terrainMinPreference.value = newValue
                terrainMinPreference.summary = prepareRatingSummary(newValue as CharSequence)
            }
            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            FILTER_TERRAIN_MIN,
            FILTER_TERRAIN_MAX -> {
                preference<ListPreference>(key).apply {
                    summary = prepareRatingSummary(entry ?: return)
                }
            }
        }
    }

    private fun prepareRatingSummary(value: CharSequence): CharSequence {
        return preparePreferenceSummary(value, 0)
    }
}
