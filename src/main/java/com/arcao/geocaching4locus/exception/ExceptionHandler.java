package com.arcao.geocaching4locus.exception;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import com.arcao.geocaching.api.data.type.MemberType;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidResponseException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.live_geocaching_api.exception.LiveGeocachingApiException;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.preference.AccountsPreferenceFragment;
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

		ErrorActivity.IntentBuilder builder = new ErrorActivity.IntentBuilder(mContext);

		if (t instanceof InvalidCredentialsException) {
			return builder.setText(R.string.error_credentials).setPreferenceFragment(AccountsPreferenceFragment.class).build();
		} else if (t instanceof InvalidResponseException) {
			return builder.setText(R.string.error_invalid_api_response).setAdditionalMessage(t.getMessage()).setException(t).build();
		} else if (t instanceof CacheNotFoundException) {
			return builder.setText(R.string.error_cache_not_found).setAdditionalMessage(((CacheNotFoundException) t).getCacheCode()).build();
		} else if (t instanceof NetworkException || t instanceof OAuthCommunicationException ||
				(t instanceof WherigoServiceException && ((WherigoServiceException) t).getCode() == WherigoServiceException.ERROR_CONNECTION_ERROR)) {
			return builder.setText(R.string.error_network).build();
		} else {
			String message = t.getMessage();
			if (message == null)
				message = "";

			return builder.setAdditionalMessage(String.format("%s<br>Exception: %s", message, t.getClass().getSimpleName())).setException(t).build();
		}
	}

	protected Intent handleLiveGeocachingApiExceptions(LiveGeocachingApiException t) {
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

				return new ErrorActivity.IntentBuilder(mContext).setTitle(resTitle).setAdditionalMessage(errorText).build();

			case NumberOfCallsExceded: // 140: too many method calls per minute
				return new ErrorActivity.IntentBuilder(mContext).setTitle(R.string.method_quota_exceeded_title).setText(R.string.method_quota_exceeded_message).build();

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
				return new ErrorActivity.IntentBuilder(mContext).setTitle(R.string.premium_member_warning_title).setText(R.string.premium_member_for_filter_required).build();

			default:
				return null;
		}
	}
}
