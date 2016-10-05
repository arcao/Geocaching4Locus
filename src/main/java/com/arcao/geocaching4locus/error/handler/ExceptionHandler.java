package com.arcao.geocaching4locus.error.handler;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import com.arcao.geocaching.api.StatusCode;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidResponseException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.exception.LiveGeocachingApiException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.util.AccountManager;
import com.arcao.geocaching4locus.authentication.util.AccountRestrictions;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.util.ResourcesUtil;
import com.arcao.geocaching4locus.error.ErrorActivity;
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException;
import com.arcao.geocaching4locus.error.exception.IntendedException;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.exception.NoResultFoundException;
import com.arcao.geocaching4locus.settings.SettingsActivity;
import com.arcao.geocaching4locus.settings.fragment.AccountsPreferenceFragment;
import com.arcao.wherigoservice.api.WherigoServiceException;
import com.github.scribejava.core.exceptions.OAuthConnectionException;
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import org.apache.commons.lang3.StringUtils;
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
		String baseMessage = "%s";

		if (t instanceof IntendedException) {
			positiveAction = ((IntendedException) t).getIntent();
			t = t.getCause();
			baseMessage = "%s<br /><br />" + ResourcesUtil.getHtmlString(mContext, R.string.error_continue_locus_map);
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
					.positiveAction(positiveAction)
					.positiveButtonText(R.string.yes_button)
					.negativeButtonText(R.string.no_button);
		}

		if (t instanceof InvalidCredentialsException) {
			return builder
					.message(R.string.error_credentials)
					.positiveAction(
							SettingsActivity.createIntent(mContext, AccountsPreferenceFragment.class))
					.positiveButtonText(R.string.ok_button)
					.negativeButtonText(null)
					.build();
		} else if (t instanceof InvalidSessionException ||
						(t instanceof LiveGeocachingApiException && ((LiveGeocachingApiException) t).getStatusCode() == StatusCode.NotAuthorized)) {
			App.get(mContext).getAccountManager().removeAccount();
			return builder
					.message(R.string.error_session_expired)
					.positiveAction(
							SettingsActivity.createIntent(mContext, AccountsPreferenceFragment.class))
					.positiveButtonText(R.string.ok_button)
					.negativeButtonText(null)
					.build();
		} else if (t instanceof InvalidResponseException) {
			return builder
					.message(baseMessage, ResourcesUtil.getHtmlString(mContext, R.string.error_invalid_api_response, t.getMessage()))
					.exception(t)
					.build();
		} else if (t instanceof CacheNotFoundException) {
			return builder
					.message(R.string.error_cache_not_found, ((CacheNotFoundException) t).getCacheCode())
					.build();
		} else if (t instanceof NetworkException || t instanceof OAuthConnectionException ||
				(t instanceof WherigoServiceException && ((WherigoServiceException) t).getCode() == WherigoServiceException.ERROR_CONNECTION_ERROR)) {
			builder.message(baseMessage, ResourcesUtil.getHtmlString(mContext, R.string.error_network));

			// Allow sending error report for exceptions that not caused by timeout or unknown host
			Throwable innerT = t.getCause();
			if (innerT != null && !(innerT instanceof InterruptedIOException) && !(innerT instanceof UnknownHostException)
					&& !(innerT instanceof ConnectException) && !(innerT instanceof EOFException) && !isSSLConnectionException(innerT)) {
				builder.exception(t);
			}

			return builder.build();
		} else if (t instanceof NoResultFoundException) {
			return builder
					.message(R.string.error_no_result)
					.build();
		} else if (t instanceof LocusMapRuntimeException) {
			t = t.getCause();
			String message = StringUtils.defaultString(t.getMessage());

			return builder
					.title(R.string.error_title_locus)
					.message(String.format("%s<br>Exception: %s", message, t.getClass().getSimpleName()))
					.exception(t)
					.build();
		} else {
			String message = StringUtils.defaultString(t.getMessage());
			return builder
					.message(baseMessage, String.format("%s<br>Exception: %s", message, t.getClass().getSimpleName()))
					.exception(t)
					.build();
		}
	}

	private boolean isSSLConnectionException(Throwable t) {
		String message = t.getMessage();

		return StringUtils.isNotEmpty(message) && (message.contains("Connection reset by peer")
				|| message.contains("Connection timed out"));
	}

	private Intent handleLiveGeocachingApiExceptions(LiveGeocachingApiException t, Intent positiveAction, String baseMessage) {
		AccountManager accountManager = App.get(mContext).getAccountManager();
		boolean premiumMember = accountManager.isPremium();
		AccountRestrictions restrictions = accountManager.getRestrictions();

		ErrorActivity.IntentBuilder builder = new ErrorActivity.IntentBuilder(mContext);

		switch (t.getStatusCode()) {
			case CacheLimitExceeded: // 118: user reach the quota limit

				int title = premiumMember ? R.string.premium_member_warning_title : R.string.basic_member_warning_title;
				int message = premiumMember ? R.string.premium_member_full_geocaching_quota_exceeded_message : R.string.basic_member_full_geocaching_quota_exceeded;

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

				String renewTime = DateFormat.getTimeFormat(mContext).format(restrictions.getRenewFullGeocacheLimit());
				String cacheString = mContext.getResources().getQuantityString(R.plurals.plurals_cache, cachesPerPeriod, cachesPerPeriod);
				String errorText = ResourcesUtil.getHtmlString(mContext, message, cacheString, periodString, renewTime);

				builder
							.title(title)
				      .message(baseMessage, errorText);

				if (positiveAction != null) {
					builder
							.positiveAction(positiveAction)
							.positiveButtonText(R.string.yes_button)
							.negativeButtonText(R.string.no_button);
				}

				return builder.build();

			case NumberOfCallsExceeded: // 140: too many method calls per minute
				builder
						.title(R.string.method_quota_exceeded_title)
						.message(baseMessage, ResourcesUtil.getHtmlString(mContext, R.string.method_quota_exceeded_message));

				if (positiveAction != null) {
					builder
							.positiveAction(positiveAction)
							.positiveButtonText(R.string.yes_button)
							.negativeButtonText(R.string.no_button);
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
				accountManager.updateAccountNextTime();
				return builder
						.title(R.string.premium_member_warning_title)
						.message(R.string.premium_member_for_filter_required)
						.build();

			default:
				return null;
		}
	}
}
