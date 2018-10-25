package com.arcao.geocaching4locus.settings.fragment

import android.content.SharedPreferences
import androidx.core.content.edit
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants.DISTANCE_KM_DEFAULT
import com.arcao.geocaching4locus.base.constants.AppConstants.DISTANCE_MILES_DEFAULT
import com.arcao.geocaching4locus.base.constants.AppConstants.MILES_PER_KILOMETER
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DISTANCE
import com.arcao.geocaching4locus.base.constants.PrefConstants.IMPERIAL_UNITS
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.base.util.getParsedFloat

class AdvancedPreferenceFragment : AbstractPreferenceFragment() {
    override val preferenceResource: Int
        get() = R.xml.preference_category_advanced

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            IMPERIAL_UNITS -> {
                val imperialUnits = sharedPreferences.getBoolean(IMPERIAL_UNITS, false)

                val defaultValue = if (imperialUnits) DISTANCE_MILES_DEFAULT else DISTANCE_KM_DEFAULT

                var distance = sharedPreferences.getParsedFloat(FILTER_DISTANCE, defaultValue)
                distance = if (imperialUnits) distance / MILES_PER_KILOMETER else distance * MILES_PER_KILOMETER

                sharedPreferences.edit {
                    putString(FILTER_DISTANCE, distance.toString())
                }
            }
        }
    }
}
