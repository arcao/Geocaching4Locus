package com.arcao.geocaching4locus.settings.fragment

import android.text.format.DateFormat
import androidx.preference.Preference
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.authentication.util.requestSignOn
import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.base.util.getText
import com.arcao.geocaching4locus.base.util.showWebPage
import com.arcao.geocaching4locus.base.util.toDate
import com.arcao.geocaching4locus.data.account.AccountManager
import org.koin.android.ext.android.inject
import java.text.NumberFormat

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

        preference<Preference>(ACCOUNT_QUOTA).apply {
            val account = accountManager.account
            if (account == null) {
                isVisible = false
            } else {
                isVisible = true
                setTitle(if (accountManager.isPremium) {
                  R.string.pref_account_quota_premium
                } else {
                  R.string.pref_account_quota_basic
                })
                summary = prepareAccountQuotaSummary()
            }
        }

        preference<Preference>(ACCOUNT_GEOCACHING_LIVE).setOnPreferenceClickListener {
            requireActivity().showWebPage(AppConstants.GEOCACHING_LIVE_URI)
            true
        }
    }

    private fun prepareAccountSummary(value: CharSequence): CharSequence {
        return requireContext().getText(R.string.pref_logout_summary, stylizedValue(value))
    }


    private fun prepareAccountQuotaSummary(): CharSequence {
        val restrictions = accountManager.restrictions()
        val renewTimeFull = DateFormat.getTimeFormat(context).format(restrictions.renewFullGeocacheQuota.toDate())
        val renewTimeLite = DateFormat.getTimeFormat(context).format(restrictions.renewLiteGeocacheQuota.toDate())

        return requireContext().getText(
            R.string.pref_account_quota_message,
            stylizedValue(restrictions.currentFullGeocacheRemaining.toFormattedString()),
            stylizedValue(restrictions.maxFullGeocacheLimit.toFormattedString()),
            stylizedValue(renewTimeFull),
            stylizedValue(restrictions.currentLiteGeocacheRemaining.toFormattedString()),
            stylizedValue(restrictions.maxLiteGeocacheLimit.toFormattedString()),
            stylizedValue(renewTimeLite)
        )
    }

    companion object {
        private const val ACCOUNT = "account"
        private const val ACCOUNT_QUOTA = "account_quota"
        private const val ACCOUNT_GEOCACHING_LIVE = "account_geocaching_live"
    }
}

private fun Int.toFormattedString(): CharSequence  = NumberFormat.getIntegerInstance().format(this)
