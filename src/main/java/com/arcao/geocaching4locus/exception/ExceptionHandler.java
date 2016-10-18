package com.arcao.geocaching4locus.exception;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import com.arcao.geocaching.api.data.type.MemberType;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidResponseException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.live_geocaching_api.StatusCode;
import com.arcao.geocaching.api.impl.live_geocaching_api.exception.LiveGeocachingApiException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.SettingsActivity;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.preference.AccountsPreferenceFragment;
import com.arcao.geocaching4locus.util.HtmlUtil;
import com.arcao.geocaching4locus.util.ResourcesUtil;
import com.arcao.wherigoservice.api.WherigoServiceException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import org.apache.commons.lang3.StringUtils;
import org.oshkimaadziig.george.androidutils.SpanFormatter;
import org.scribe.exceptions.OAuthConnectionException;
import timber.log.Timber;
public class ExceptionHandler {
	private final Context mContext;

	public ExceptionHandler(@NonNull Context context) {
		mContext = context;
	}

	@NonNull
	public Intent handle(@NonNull Throwable t) {
		Timber.e(t, t.getMessage());

		Intent positiveAction = null;
		CharSequence baseMessage = "%s";

		if (t instanceof IntendedException) {
			positiveAction = ((IntendedException) t).getIntent();
			t = t.getCause();
			baseMessage = SpanFormatter.format(HtmlUtil.fromHtml("%%s<br /><br />%s"), mContext.getText(R.string.error_continue_locus_map));
		}

		// special handling for some API exceptions
		if (t instanceof LiveGeocachingApiException) {
			Intent intent = handleLiveGeocachingApiExceptions((LiveGeocachingApiException) t, positiveAction, baseMessage);
			if (intent != null)
				return intent;
		}

		ErrorActivity.IntentBuilder builder = new ErrorActivity.IntentBuilder(mContext);
		if (positiveAction != null) {
			builder
					.setPositiveAction(positiveAction)
					.setPositiveButtonText(R.string.yes_button)
					.setNegativeButtonText(R.string.no_button);
		}

		if (t instanceof InvalidCredentialsException) {
			return builder
					.setMessage(R.string.error_credentials)
					.setPositiveAction(
							SettingsActivity.createIntent(mContext, AccountsPreferenceFragment.class))
					.setPositiveButtonText(R.string.ok_button)
					.setNegativeButtonText(0)
					.build();
		} else if (t instanceof InvalidSessionException ||
						(t instanceof LiveGeocachingApiException && ((LiveGeocachingApiException) t).getStatusCode() == StatusCode.NotAuthorized)) {
			App.get(mContext).getAuthenticatorHelper().removeAccount();
			return builder
					.setMessage(R.string.error_session_expired)
					.setPositiveAction(
							SettingsActivity.createIntent(mContext, AccountsPreferenceFragment.class))
					.setPositiveButtonText(R.string.ok_button)
					.setNegativeButtonText(0)
					.build();
		} else if (t instanceof InvalidResponseException) {
			return builder
					.setMessage(baseMessage, ResourcesUtil.getText(mContext, R.string.error_invalid_api_response, t.getMessage()))
					.setException(t)
					.build();
		} else if (t instanceof CacheNotFoundException) {
			return builder
					.setMessage(R.string.error_cache_not_found, ((CacheNotFoundException) t).getCacheCode())
					.build();
		} else if (t instanceof NetworkException || t instanceof OAuthConnectionException ||
				(t instanceof WherigoServiceException && ((WherigoServiceException) t).getCode() == WherigoServiceException.ERROR_CONNECTION_ERROR)) {
			builder.setMessage(baseMessage, mContext.getText(R.string.error_network));

			// Allow sending error report for exceptions that not caused by timeout or unknown host
			Throwable innerT = t.getCause();
			if (innerT != null && !(innerT instanceof InterruptedIOException) && !(innerT instanceof UnknownHostException)
					&& !(innerT instanceof ConnectException) && !isSSLConnectionException(innerT)) {
				builder.setException(t);
			}

			return builder.build();
		} else if (t instanceof NoResultFoundException) {
			return builder
					.setMessage(R.string.error_no_result)
					.build();
		} else if (t instanceof LocusMapRuntimeException) {
			t = t.getCause();
			String message = StringUtils.defaultString(t.getMessage());

			return builder
					.setTitle(R.string.error_title_locus)
					.setMessage(SpanFormatter.format(HtmlUtil.fromHtml("%s<br>Exception: %s"), message, t.getClass().getSimpleName()))
					.setException(t)
					.build();
		} else {
			String message = StringUtils.defaultString(t.getMessage());
			return builder
					.setMessage(baseMessage, SpanFormatter.format(HtmlUtil.fromHtml("%s<br>Exception: %s"), message, t.getClass().getSimpleName()))
					.setException(t)
					.build();
		}
	}

	private boolean isSSLConnectionException(Throwable t) {
		String message = t.getMessage();

		return StringUtils.isNotEmpty(message) && (message.contains("Connection reset by peer")
				|| message.contains("Connection timed out"));
	}

	private Intent handleLiveGeocachingApiExceptions(LiveGeocachingApiException t, Intent positiveAction, CharSequence baseMessage) {
		AccountRestrictions restrictions = App.get(mContext).getAuthenticatorHelper().getRestrictions();
		ErrorActivity.IntentBuilder builder = new ErrorActivity.IntentBuilder(mContext);

		switch (t.getStatusCode()) {
			case CacheLimitExceeded: // 118: user reach the quota limit

				int title = restrictions.isPremiumMember() ? R.string.premium_member_warning_title : R.string.basic_member_warning_title;
				int message = restrictions.isPremiumMember() ? R.string.premium_member_full_geocaching_quota_exceeded_message : R.string.basic_member_full_geocaching_quota_exceeded;

				// apply format on a text
				int cachesPerPeriod = (int) restrictions.getMaxFullGeocacheLimit();
				int period = (int) restrictions.getFullGeocacheLimitPeriod();

				String periodString;
				if (period < AppConstants.SECONDS_PER_MINUTE) {
					periodString = mContext.getResources().getQuantityString(R.plurals.plurals_minute, period, period);
				} else {
					period /= AppConstants.SECONDS_PER_MINUTE;
					periodString = mContext.getResources().getQuantityString(R.plurals.plurals_hour, period, period);
				}

				CharSequence renewTime = DateFormat.getTimeFormat(mContext).format(restrictions.getRenewFullGeocacheLimit());
				CharSequence cacheString = ResourcesUtil.getQuantityText(mContext, R.plurals.plurals_cache, cachesPerPeriod, cachesPerPeriod);
				CharSequence errorText = ResourcesUtil.getText(mContext, message, cacheString, periodString, renewTime);

				builder
							.setTitle(title)
				      .setMessage(baseMessage, errorText);

				if (positiveAction != null) {
					builder
							.setPositiveAction(positiveAction)
							.setPositiveButtonText(R.string.yes_button)
							.setNegativeButtonText(R.string.no_button);
				}

				return builder.build();

			case NumberOfCallsExceeded: // 140: too many method calls per minute
				builder
						.setTitle(R.string.method_quota_exceeded_title)
						.setMessage(baseMessage, mContext.getText(R.string.method_quota_exceeded_message));

				if (positiveAction != null) {
					builder
							.setPositiveAction(positiveAction)
							.setPositiveButtonText(R.string.yes_button)
							.setNegativeButtonText(R.string.no_button);
				}

				return builder.build();

			case PremiumMembershipRequiredForBookmarksExcludeFilter:
			case PremiumMembershipRequiredForDifficultyFilter:
			case PremiumMembershipRequiredForFavoritePointsFilter:
			case PremiumMembershipRequiredForGeocacheContainerSizeFilter:
			case PremiumMembershipRequiredForGeocacheNameFilter:
			case PremiumMembershipRequiredForHiddenByUserFilter:
			case PremiumMembershipRequiredForNotHiddenByUserFilter:
			case PremiumMembershipRequiredForTerrainFilter:
			case PremiumMembershipRequiredForTrackableCountFilter:
				restrictions.updateMemberType(MemberType.Basic);
				return builder
						.setTitle(R.string.premium_member_warning_title)
						.setMessage(R.string.premium_member_for_filter_required)
						.build();

			default:
				return null;
		}
	}
}
