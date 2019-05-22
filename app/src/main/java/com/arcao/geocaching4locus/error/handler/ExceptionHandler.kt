package com.arcao.geocaching4locus.error.handler

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.text.format.DateFormat
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountRestrictions
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.AccountNotFoundException
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.util.HtmlUtil
import com.arcao.geocaching4locus.base.util.getQuantityText
import com.arcao.geocaching4locus.base.util.getText
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.exception.AuthenticationException
import com.arcao.geocaching4locus.data.api.exception.GeocachingApiException
import com.arcao.geocaching4locus.data.api.exception.InvalidResponseException
import com.arcao.geocaching4locus.data.api.model.enum.StatusCode
import com.arcao.geocaching4locus.error.ErrorActivity
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException
import com.arcao.geocaching4locus.error.exception.NoResultFoundException
import com.arcao.geocaching4locus.settings.SettingsActivity
import com.arcao.geocaching4locus.settings.fragment.AccountsPreferenceFragment
import com.github.scribejava.core.exceptions.OAuthException
import org.oshkimaadziig.george.androidutils.SpanFormatter
import timber.log.Timber
import java.io.EOFException
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException

class ExceptionHandler(private val context: Context, private val accountManager: AccountManager) {
    operator fun invoke(throwable: Throwable): Intent {
        var t = throwable
        Timber.e(t)

        var positiveAction: Intent? = null
        var baseMessage: CharSequence = "%s"

        if (t is IntendedException) {
            positiveAction = t.intent
            t = t.cause!!

            baseMessage = SpanFormatter.format(
                HtmlUtil.fromHtml("%%s<br /><br />%s"),
                context.getText(R.string.error_continue_locus_map)
            )
        }

        // special handling for some API exceptions
        if (t is GeocachingApiException) {
            val intent = handleGeocachingApiExceptions(t, positiveAction, baseMessage)
            if (intent != null)
                return intent
        }

        val builder = ErrorActivity.IntentBuilder(context)
        if (positiveAction != null) {
            builder.positiveAction(positiveAction)
                .positiveButtonText(R.string.button_yes)
                .negativeButtonText(R.string.button_no)
        }

        if (t is AuthenticationException || t is AccountNotFoundException) {
            accountManager.deleteAccount()
            return builder.message(R.string.error_no_account)
                .positiveAction(
                    SettingsActivity.createIntent(context, AccountsPreferenceFragment::class.java)
                )
                .positiveButtonText(R.string.button_ok)
                .clearNegativeButtonText()
                .build()
        } else if (t is InvalidResponseException) {
            return builder.message(baseMessage, context.getText(R.string.error_invalid_api_response, t.message!!))
                .exception(t)
                .build()
        } else if (t is CacheNotFoundException) {
            val geocacheCodes = t.cacheCodes
            return builder.message(
                baseMessage,
                context.getQuantityText(
                    R.plurals.plural_error_geocache_not_found,
                    geocacheCodes.size, TextUtils.join(", ", geocacheCodes)
                )
            ).build()
        } else if (t is IOException) {
            builder.message(baseMessage, context.getText(R.string.error_network_unavailable))

            // Allow sending error report for exceptions that not caused by timeout or unknown host
            if (t !is InterruptedIOException && t !is UnknownHostException &&
                t !is ConnectException && t !is EOFException && !isSSLConnectionException(t)
            ) {
                builder.exception(t)
            }

            return builder.build()
        } else if (t is NoResultFoundException) {
            return builder.message(R.string.error_no_result)
                .build()
        } else if (t is LocusMapRuntimeException) {
            val cause = t.cause!!
            val message = t.message.orEmpty()

            return builder
                .title(R.string.title_locus_map_error)
                .message(
                    SpanFormatter.format(
                        HtmlUtil.fromHtml("%s<br>Exception: %s"),
                        message,
                        cause.javaClass.simpleName
                    )
                )
                .exception(cause)
                .build()
        } else if (t is FileNotFoundException || t.cause is FileNotFoundException) {
            return builder
                .message(R.string.error_no_write_file_permission)
                .build()
        } else if (t is OAuthException && t.message == "oauth_verifier argument was incorrect.") {
            return builder
                .message(R.string.error_invalid_authorization_code)
                .build()
        } else {
            val message = t.message.orEmpty()
            return builder
                .message(
                    baseMessage,
                    SpanFormatter.format(HtmlUtil.fromHtml("%s<br>Exception: %s"), message, t.javaClass.simpleName)
                )
                .exception(t)
                .build()
        }
    }

    private fun isSSLConnectionException(t: Throwable): Boolean {
        val message = t.message

        return !message.isNullOrEmpty() && (message.contains("Connection reset by peer") || message.contains("Connection timed out"))
    }

    private fun handleGeocachingApiExceptions(
        t: GeocachingApiException,
        positiveAction: Intent?,
        baseMessage: CharSequence
    ): Intent? {
        // TODO fix this
        val premiumMember = accountManager.isPremium
        val restrictions = accountManager.restrictions()

        val builder = ErrorActivity.IntentBuilder(context)

        when (t.statusCode) {
            // user reach the quota limit
            StatusCode.CONFLICT -> {
                val title = if (premiumMember)
                    R.string.title_premium_member_warning
                else
                    R.string.title_basic_member_warning

                val message = if (premiumMember)
                    R.string.error_premium_member_full_geocaching_quota_exceeded
                else
                    R.string.error_basic_member_full_geocaching_quota_exceeded

                // apply format on a text
                val cachesPerPeriod = restrictions.maxFullGeocacheLimit
                var period = AccountRestrictions.DEFAULT_RENEW_DURATION.seconds.toInt()

                val periodString: String
                if (period < AppConstants.SECONDS_PER_MINUTE) {
                    periodString = context.resources.getQuantityString(R.plurals.plurals_minute, period, period)
                } else {
                    period /= AppConstants.SECONDS_PER_MINUTE
                    periodString = context.resources.getQuantityString(R.plurals.plurals_hour, period, period)
                }

                val renewTime = DateFormat.getTimeFormat(context).format(restrictions.renewFullGeocacheLimit)
                val cacheString = context.getQuantityText(R.plurals.plurals_geocache, cachesPerPeriod, cachesPerPeriod)
                val errorText = context.getText(message, cacheString, periodString, renewTime)

                builder.title(title).message(baseMessage, errorText)

                if (positiveAction != null) {
                    builder.positiveAction(positiveAction)
                        .positiveButtonText(R.string.button_yes)
                        .negativeButtonText(R.string.button_no)
                }

                return builder.build()
            }

            // 140: too many method calls per minute
            StatusCode.TOO_MANY_REQUESTS -> {
                builder.title(R.string.title_method_quota_exceeded)
                    .message(baseMessage, context.getText(R.string.error_method_quota_exceeded))

                if (positiveAction != null) {
                    builder.positiveAction(positiveAction)
                        .positiveButtonText(R.string.button_yes)
                        .negativeButtonText(R.string.button_no)
                }

                return builder.build()
            }

//            // BM user use PM filters
//            StatusCode.PremiumMembershipRequiredForBookmarksExcludeFilter,
//            StatusCode.PremiumMembershipRequiredForDifficultyFilter,
//            StatusCode.PremiumMembershipRequiredForFavoritePointsFilter,
//            StatusCode.PremiumMembershipRequiredForGeocacheContainerSizeFilter,
//            StatusCode.PremiumMembershipRequiredForGeocacheNameFilter,
//            StatusCode.PremiumMembershipRequiredForHiddenByUserFilter,
//            StatusCode.PremiumMembershipRequiredForNotHiddenByUserFilter,
//            StatusCode.PremiumMembershipRequiredForTerrainFilter,
//            StatusCode.PremiumMembershipRequiredForTrackableCountFilter -> {
//                accountManager.updateAccountNextTime()
//                return builder.title(R.string.title_premium_member_warning)
//                    .message(R.string.error_premium_filter)
//                    .build()
//            }

            else -> return null
        }
    }
}
