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
import com.arcao.geocaching4locus.base.constants.PrefConstants.ABOUT_VERSION
import com.arcao.geocaching4locus.base.constants.PrefConstants.ABOUT_WEBSITE
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment
import com.arcao.geocaching4locus.base.fragment.AbstractPreferenceFragment
import com.arcao.geocaching4locus.base.util.showWebPage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class AboutPreferenceFragment : AbstractPreferenceFragment(), CoroutineScope {
    private val dispatcherProvider by inject<CoroutinesDispatcherProvider>()
    private val feedbackHelper by inject<FeedbackHelper>()

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + dispatcherProvider.main

    override val preferenceResource: Int
        get() = R.xml.preference_category_about

    override fun preparePreference() {
        super.preparePreference()

        preference<Preference>(ABOUT_VERSION).summary =
            "${get<App>().version} (${BuildConfig.GIT_SHA})"

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

        preference<Preference>(ABOUT_FEEDBACK).setOnPreferenceClickListener {
            launch {
                val intent = feedbackHelper.createFeedbackIntent(
                    R.string.feedback_email,
                    R.string.feedback_subject,
                    R.string.feedback_body
                )

                startActivityForResult(intent, REQ_FEEDBACK)
            }
            true
        }

        preference<Preference>(ABOUT_DONATE_PAYPAL).setOnPreferenceClickListener {
            DonatePaypalDialogFragment().show(requireFragmentManager(), DonatePaypalDialogFragment.TAG)
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
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

    companion object {
        const val REQ_FEEDBACK = 1
    }
}
