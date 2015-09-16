package com.arcao.geocaching4locus.exception;

import android.content.Context;
import android.content.Intent;
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
import com.arcao.wherigoservice.api.WherigoServiceException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import org.apache.commons.lang3.StringUtils;
import org.scribe.exceptions.OAuthConnectionException;
import timber.log.Timber;

public class ExceptionHandler {
	private final Context mContext;

	public ExceptionHandler(Context context) {
		mContext = context;
	}

	public Intent handle(Throwable t) {
		Timber.e(t, t.getMessage());

		Intent nextAction = null;

		if (t instanceof IntentedException) {
			nextAction = ((IntentedException) t).getIntent();
			t = t.getCause();
		}

		// special handling for some API exceptions
		if (t instanceof LiveGeocachingApiException) {
			Intent intent = handleLiveGeocachingApiExceptions((LiveGeocachingApiException) t, nextAction);
			if (intent != null)
				return intent;
		}

		ErrorActivity.IntentBuilder builder = new ErrorActivity.IntentBuilder(mContext);
		if (nextAction != null) {
			builder.setNextAction(nextAction);
		}

		if (t instanceof InvalidCredentialsException) {
			return builder
					.setText(R.string.error_credentials)
					.setNextAction(SettingsActivity.createIntent(mContext, AccountsPreferenceFragment.class))
					.setNextActionText(R.string.ok_button)
					.build();
		} else if (t instanceof InvalidSessionException ||
						(t instanceof LiveGeocachingApiException && ((LiveGeocachingApiException) t).getStatusCode() == StatusCode.NotAuthorized)) {
			App.get(mContext).getAuthenticatorHelper().removeAccount();
			return builder
					.setText(R.string.error_session_expired)
					.setNextAction(SettingsActivity.createIntent(mContext, AccountsPreferenceFragment.class))
					.setNextActionText(R.string.ok_button)
					.build();
		} else if (t instanceof InvalidResponseException) {
			return builder
					.setText(R.string.error_invalid_api_response)
					.setAdditionalMessage(t.getMessage())
					.setException(t)
					.build();
		} else if (t instanceof CacheNotFoundException) {
			return builder
					.setText(R.string.error_cache_not_found)
					.setAdditionalMessage(((CacheNotFoundException) t).getCacheCode())
					.build();
		} else if (t instanceof NetworkException || t instanceof OAuthConnectionException ||
				(t instanceof WherigoServiceException && ((WherigoServiceException) t).getCode() == WherigoServiceException.ERROR_CONNECTION_ERROR)) {
			builder.setText(R.string.error_network);

			// Allow sending error report for exceptions that not caused by timeout or unknown host
			Throwable innerT = t.getCause();
			if (innerT != null && !(innerT instanceof InterruptedIOException) && !(innerT instanceof UnknownHostException)) {
				builder.setException(t);
			}

			return builder.build();
		} else if (t instanceof NoResultFoundException) {
			return builder
					.setText(R.string.error_no_result)
					.build();
		} else if (t instanceof LocusMapRuntimeException) {
			t = t.getCause();
			String message = StringUtils.defaultString(t.getMessage());

			return builder
					.setTitle(R.string.error_title_locus)
					.setAdditionalMessage(
							String.format("%s<br>Exception: %s", message, t.getClass().getSimpleName()))
					.setException(t)
					.build();
		} else {
			String message = StringUtils.defaultString(t.getMessage());
			return builder
					.setAdditionalMessage(
							String.format("%s<br>Exception: %s", message, t.getClass().getSimpleName()))
					.setException(t)
					.build();
		}
	}

	protected Intent handleLiveGeocachingApiExceptions(LiveGeocachingApiException t, Intent nextAction) {
		AccountRestrictions restrictions = App.get(mContext).getAuthenticatorHelper().getRestrictions();

		switch (t.getStatusCode()) {
			case CacheLimitExceeded: // 118: user reach the quota limit

				int resTitle = (restrictions.isPremiumMember()) ? R.string.premium_member_warning_title : R.string.basic_member_warning_title;
				int resText = (restrictions.isPremiumMember()) ? R.string.premium_member_full_geocaching_quota_exceeded_message : R.string.basic_member_full_geocaching_quota_exceeded;

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
				String errorText = mContext.getString(resText, cacheString, periodString, renewTime);

				return new ErrorActivity.IntentBuilder(mContext)
						.setTitle(resTitle)
						.setAdditionalMessage(errorText)
						.setNextAction(nextAction)
						.build();

			case NumberOfCallsExceeded: // 140: too many method calls per minute
				return new ErrorActivity.IntentBuilder(mContext)
						.setTitle(R.string.method_quota_exceeded_title)
						.setText(R.string.method_quota_exceeded_message)
						.setNextAction(nextAction)
						.build();

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
				return new ErrorActivity.IntentBuilder(mContext)
						.setTitle(R.string.premium_member_warning_title)
						.setText(R.string.premium_member_for_filter_required)
						.build();

			default:
				return null;
		}
	}
}
