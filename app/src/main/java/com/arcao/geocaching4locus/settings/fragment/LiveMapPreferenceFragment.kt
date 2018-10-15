package com.arcao.geocaching4locus.settings.fragment

import android.os.Bundle
import androidx.preference.CheckBoxPreference
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.PrefConstants.LIVE_MAP_DOWNLOAD_HINTS
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment

class LiveMapPreferenceFragment : AbstractPreferenceFragment() {
    private var premiumMember: Boolean = false

    override val preferenceResource: Int
        get() = R.xml.preference_category_live_map

    override fun onCreatePreferences(savedInstanceState: Bundle, rootKey: String) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        premiumMember = App.get(activity).accountManager.isPremium
    }

    override fun preparePreference() {
        super.preparePreference()

        preference<CheckBoxPreference>(LIVE_MAP_DOWNLOAD_HINTS).apply {
            isEnabled = premiumMember

            if (!premiumMember) {
                applyPremiumTitleSign(this)
            }
        }
    }
}
