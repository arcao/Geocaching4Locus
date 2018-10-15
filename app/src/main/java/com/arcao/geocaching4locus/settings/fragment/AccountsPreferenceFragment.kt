package com.arcao.geocaching4locus.settings.fragment


import androidx.preference.Preference
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants.ACCOUNT_GEOCACHING_LIVE
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.base.util.IntentUtil
import com.arcao.geocaching4locus.base.util.ResourcesUtil

class AccountsPreferenceFragment : AbstractPreferenceFragment() {
    override val preferenceResource: Int
        get() = R.xml.preference_category_accounts

    override fun preparePreference() {
        val accountManager = App.get(activity).accountManager

        preference<Preference>(ACCOUNT).apply {
            setOnPreferenceClickListener {
                if (accountManager.account != null) {
                    accountManager.removeAccount()
                    setTitle(R.string.pref_login)
                    setSummary(R.string.pref_login_summary)
                } else {
                    accountManager.requestSignOn(requireActivity(), 0)
                }

                true
            }

            val account = accountManager.account
            if (account != null) {
                setTitle(R.string.pref_logout)
                summary = prepareAccountSummary(account.name())
            } else {
                setTitle(R.string.pref_login)
                setSummary(R.string.pref_login_summary)
            }
        }

        preference<Preference>(ACCOUNT_GEOCACHING_LIVE).setOnPreferenceClickListener {
            IntentUtil.showWebPage(activity, AppConstants.GEOCACHING_LIVE_URI)
            true
        }
    }

    private fun prepareAccountSummary(value: CharSequence): CharSequence {
        return ResourcesUtil.getText(requireContext(), R.string.pref_logout_summary, stylizedValue(value))
    }

    companion object {
        private const val ACCOUNT = "account"
    }
}
