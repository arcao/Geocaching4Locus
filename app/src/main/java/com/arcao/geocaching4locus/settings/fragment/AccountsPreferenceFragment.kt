package com.arcao.geocaching4locus.settings.fragment

import androidx.preference.Preference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.requestSignOn
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants.ACCOUNT_POWERED_BY
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.base.util.getText
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.data.account.AccountManager
import org.koin.android.ext.android.inject

class AccountsPreferenceFragment : AbstractPreferenceFragment() {
    val accountManager by inject<AccountManager>()

    override val preferenceResource: Int
        get() = R.xml.preference_category_accounts

    override fun preparePreference() {
        super.preparePreference()

        preference<Preference>(ACCOUNT).apply {
            setOnPreferenceClickListener {
                if (accountManager.account != null) {
                    accountManager.deleteAccount()
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
                summary = prepareAccountSummary(account.userName.orEmpty())
            } else {
                setTitle(R.string.pref_login)
                setSummary(R.string.pref_login_summary)
            }
        }

        preference<Preference>(ACCOUNT_POWERED_BY).setOnPreferenceClickListener {
            requireActivity().showWebPage(AppConstants.GEOCACHING_URI)
            true
        }
    }

    private fun prepareAccountSummary(value: CharSequence): CharSequence {
        return requireContext().getText(R.string.pref_logout_summary, stylizedValue(value))
    }

    companion object {
        private const val ACCOUNT = "account"
    }
}
