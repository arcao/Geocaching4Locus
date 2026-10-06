package com.arcao.geocaching4locus.settings.fragment

import androidx.preference.Preference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.LoginActivity
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants.ACCOUNT
import com.arcao.geocaching4locus.base.constants.PrefConstants.ACCOUNT_POWERED_BY
import com.arcao.geocaching4locus.base.constants.PrefConstants.ACCOUNT_REFRESH
import com.arcao.geocaching4locus.base.constants.PrefConstants.MEMBERSHIP_TYPE
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.base.util.getText
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.model.enums.MembershipType
import org.koin.android.ext.android.inject

class AccountsPreferenceFragment : AbstractPreferenceFragment() {
    val accountManager by inject<AccountManager>()

    private val loginActivity = registerForActivityResult(LoginActivity.Contract) {}

    override val preferenceResource: Int
        get() = R.xml.preference_category_accounts

    override fun preparePreference() {
        super.preparePreference()

        val account = accountManager.account

        preference<Preference>(ACCOUNT).apply {
            setOnPreferenceClickListener {
                if (accountManager.account != null) {
                    accountManager.deleteAccount()
                    setTitle(R.string.pref_login)
                    setSummary(R.string.pref_login_summary)

                    preference<Preference>(MEMBERSHIP_TYPE).isVisible = false
                    preference<Preference>(ACCOUNT_REFRESH).isVisible = false
                } else {
                    loginActivity.launch(null)
                }

                true
            }

            if (account != null) {
                setTitle(R.string.pref_logout)
                summary = prepareAccountSummary(account.userName.orEmpty())
            } else {
                setTitle(R.string.pref_login)
                setSummary(R.string.pref_login_summary)
            }
        }

        preference<Preference>(MEMBERSHIP_TYPE).apply {
            isVisible = account != null

            if (account != null) {
                summary = stylizedValue(getString(account.membership.toLocalizedName()))
            }
        }

        preference<Preference>(ACCOUNT_REFRESH).apply {
            isVisible = account != null

            setOnPreferenceClickListener {
                loginActivity.launch(null)
                true
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
}

private fun MembershipType.toLocalizedName(): Int = when (this) {
    MembershipType.UNKNOWN -> R.string.membership_unknown
    MembershipType.BASIC -> R.string.membership_basic
    MembershipType.CHARTER -> R.string.membership_charter
    MembershipType.PREMIUM -> R.string.membership_premium
}
