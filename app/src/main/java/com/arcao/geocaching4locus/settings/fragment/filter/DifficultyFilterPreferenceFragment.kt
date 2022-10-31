package com.arcao.geocaching4locus.settings.fragment.filter

import android.content.SharedPreferences
import androidx.preference.ListPreference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DIFFICULTY_MAX
import com.arcao.geocaching4locus.base.constants.PrefConstants.FILTER_DIFFICULTY_MIN
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment

class DifficultyFilterPreferenceFragment : AbstractPreferenceFragment() {
    override val preferenceResource: Int
        get() = R.xml.preference_category_filter_difficulty

    override fun preparePreference() {
        super.preparePreference()

        val difficultyMinPreference = preference<ListPreference>(FILTER_DIFFICULTY_MIN)
        val difficultyMaxPreference = preference<ListPreference>(FILTER_DIFFICULTY_MAX)

        difficultyMinPreference.summary = prepareRatingSummary(difficultyMinPreference.value)
        difficultyMinPreference.setOnPreferenceChangeListener { _, newValue ->
            val min = (newValue as String).toFloat()
            val max = difficultyMaxPreference.value.toFloat()

            if (min > max) {
                difficultyMaxPreference.value = newValue
                difficultyMaxPreference.summary = prepareRatingSummary(newValue as CharSequence)
            }
            true
        }

        difficultyMaxPreference.summary = prepareRatingSummary(difficultyMaxPreference.value)
        difficultyMaxPreference.setOnPreferenceChangeListener { _, newValue ->
            val min = difficultyMinPreference.value.toFloat()
            val max = (newValue as String).toFloat()

            if (min > max) {
                difficultyMinPreference.value = newValue
                difficultyMinPreference.summary = prepareRatingSummary(newValue as CharSequence)
            }
            true
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)

        when (key) {
            FILTER_DIFFICULTY_MIN,
            FILTER_DIFFICULTY_MAX -> {
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
