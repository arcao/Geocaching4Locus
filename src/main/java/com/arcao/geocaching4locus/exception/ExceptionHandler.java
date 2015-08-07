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
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.wherigoservice.api.WherigoServiceException;

import oauth.signpost.exception.OAuthCommunicationException;

public class ExceptionHandler {
	protected final Context mContext;

	public ExceptionHandler(Context ctx) {
		mContext = ctx;
	}

	public Intent handle(Throwable t) {
		// special handling for some API exceptions
		if (t instanceof LiveGeocachingApiException) {
			Intent intent = handleLiveGeocachingApiExceptions((LiveGeocachingApiException) t);
			if (intent != null)
				return intent;
		}


		if (t instanceof InvalidCredentialsException) {
			return ErrorActivity.createErrorIntent(mContext, R.string.error_credentials, null, true, null);
		} else if (isSessionError(t)) {
			Geocaching4LocusApplication.getAuthenticatorHelper().removeAccount();
			return ErrorActivity.createErrorIntent(mContext, R.string.error_session_expired, null, true, null);
		} else if (t instanceof InvalidResponseException) {
			return ErrorActivity.createErrorIntent(mContext, R.string.error_invalid_api_response, t.getMessage(), false, t);
		} else if (t instanceof CacheNotFoundException) {
			return ErrorActivity.createErrorIntent(mContext, R.string.error_cache_not_found, ((CacheNotFoundException) t).getCacheCode(), false, null);
		} else if (t instanceof NetworkException || t instanceof OAuthCommunicationException ||
				(t instanceof WherigoServiceException && ((WherigoServiceException) t).getCode() == WherigoServiceException.ERROR_CONNECTION_ERROR)) {
			return ErrorActivity.createErrorIntent(mContext, R.string.error_network, null, false, null);
		} else {
			String message = t.getMessage();
			if (message == null)
				message = "";

			return ErrorActivity.createErrorIntent(mContext, 0, String.format("%s<br>Exception: %s", message, t.getClass().getSimpleName()), false, t);
		}
	}

	private boolean isSessionError(Throwable t) {
		if (t instanceof InvalidSessionException)
			return true;

		if (t instanceof LiveGeocachingApiException) {
			switch (((LiveGeocachingApiException) t).getStatusCode()) {
				case NotAuthorized:
				case UserAccountProblem:
					return true;
			}
		}

		return false;
	}

	protected Intent handleLiveGeocachingApiExceptions(LiveGeocachingApiException t) {
		AccountRestrictions restrictions = Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions();

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
					period = period / AppConstants.SECONDS_PER_MINUTE;
					periodString = mContext.getResources().getQuantityString(R.plurals.plurals_hour, period, period);
				}

				String renewTime = DateFormat.getTimeFormat(mContext).format(restrictions.getRenewFullGeocacheLimit());

				String cacheString = mContext.getResources().getQuantityString(R.plurals.plurals_cache, cachesPerPeriod, cachesPerPeriod);
				String errorText = mContext.getString(resText, cacheString, periodString, renewTime);

				return ErrorActivity.createErrorIntent(mContext, resTitle, 0, errorText, false, null);

			case NumberOfCallsExceded: // 140: too many method calls per minute
				return ErrorActivity.createErrorIntent(mContext, R.string.method_quota_exceeded_title, R.string.method_quota_exceeded_message, null, false, null);

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
				return ErrorActivity.createErrorIntent(mContext, R.string.premium_member_warning_title, R.string.premium_member_for_filter_required, null, false, null);

			default:
				return null;
		}
	}
}
