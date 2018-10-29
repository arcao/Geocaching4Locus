package com.arcao.geocaching4locus.settings.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.preference.Preference
import com.arcao.feedback.FeedbackHelper
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.BuildConfig
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants.ABOUT_DONATE_PAYPAL
import com.arcao.geocaching4locus.base.constants.PrefConstants.ABOUT_FACEBOOK
import com.arcao.geocaching4locus.base.constants.PrefConstants.ABOUT_FEEDBACK
import com.arcao.geocaching4locus.base.constants.PrefConstants.ABOUT_GPLUS
import com.arcao.geocaching4locus.base.constants.PrefConstants.ABOUT_VERSION
import com.arcao.geocaching4locus.base.constants.PrefConstants.ABOUT_WEBSITE
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.base.util.showWebPage

class AboutPreferenceFragment : AbstractPreferenceFragment() {
    override val preferenceResource: Int
        get() = R.xml.preference_category_about

    override fun preparePreference() {
        preference<Preference>(ABOUT_VERSION).summary =
                "${App[requireContext()].version} (${BuildConfig.GIT_SHA})"

        preference<Preference>(ABOUT_WEBSITE).apply {
            summary = AppConstants.WEBSITE_URI.toString()
            setOnPreferenceClickListener {
                requireActivity().showWebPage(AppConstants.WEBSITE_URI)
            }
        }

        preference<Preference>(ABOUT_FACEBOOK).apply {
            summary = AppConstants.FACEBOOK_URI.toString()
            setOnPreferenceClickListener {
                requireActivity().showWebPage(AppConstants.FACEBOOK_URI)
            }
        }

        preference<Preference>(ABOUT_GPLUS).apply {
            summary = AppConstants.GPLUS_URI.toString()
            setOnPreferenceClickListener {
                requireActivity().showWebPage(AppConstants.GPLUS_URI)
            }
        }

        preference<Preference>(ABOUT_FEEDBACK).setOnPreferenceClickListener {
            FeedbackHelper.sendFeedback(requireActivity(), R.string.feedback_email, R.string.feedback_subject, R.string.feedback_body)
            true
        }

        preference<Preference>(ABOUT_DONATE_PAYPAL).setOnPreferenceClickListener {
            DonatePaypalDialogFragment().show(requireFragmentManager(), DonatePaypalDialogFragment.TAG)
            true
        }
    }

    class DonatePaypalDialogFragment : AbstractDialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.pref_donate_paypal_choose_currency)
                    .setItems(R.array.currency) { _, which ->
                        requireActivity().showWebPage(
                                AppConstants.DONATE_PAYPAL_URI.format(resources.getStringArray(R.array.currency)[which]).toUri()
                        )
                    }
                    .setCancelable(true)
                    .create()
        }

        companion object {
            const val TAG = "DonatePaypalDialogFragment"
        }
    }
}
