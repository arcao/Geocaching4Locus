package com.arcao.geocaching4locus.settings.fragment

import androidx.preference.CheckBoxPreference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.constants.PrefConstants.LIVE_MAP_DOWNLOAD_HINTS
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.data.account.AccountManager
import org.koin.android.ext.android.get

class LiveMapPreferenceFragment : AbstractPreferenceFragment() {
    private val premiumMember: Boolean by lazy {
        get<AccountManager>().isPremium
    }

    override val preferenceResource: Int
        get() = R.xml.preference_category_live_map

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
