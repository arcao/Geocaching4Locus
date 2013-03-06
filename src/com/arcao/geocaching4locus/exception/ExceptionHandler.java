package com.arcao.geocaching4locus.exception;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;

import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.live_geocaching_api.exception.LiveGeocachingApiException;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.wherigoservice.api.WherigoServiceException;

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
		} else if (t instanceof CacheNotFoundException) {
			return ErrorActivity.createErrorIntent(mContext, R.string.error_cache_not_found, ((CacheNotFoundException) t).getCacheCode(), false, null);
		} else if (t instanceof NetworkException || 
				(t instanceof WherigoServiceException && ((WherigoServiceException) t).getCode() == WherigoServiceException.ERROR_CONNECTION_ERROR)) {
			return ErrorActivity.createErrorIntent(mContext, R.string.error_network, null, false, null);
		} else {
			String message = t.getMessage();
			if (message == null)
				message = "";

			return ErrorActivity.createErrorIntent(mContext, 0, String.format("%s<br>Exception: %s", message, t.getClass().getSimpleName()), false, t);
		}
	}

	protected Intent handleLiveGeocachingApiExceptions(LiveGeocachingApiException t) {
		switch (t.getStatusCode()) {
			case CacheLimitExceeded: // 118: user reach the quota limit
				AccountRestrictions restrictions = Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions();
				
				int resTitle = (restrictions.isPremiumMember()) ? R.string.premium_member_warning_title : R.string.basic_member_warning_title;
				int resText = (restrictions.isPremiumMember()) ? R.string.premium_member_full_geocaching_quota_exceeded_message : R.string.basic_member_full_geocaching_quota_exceeded;
				
				// apply format on a text
				int cachesPerPeriod = (int) restrictions.getMaxFullGeocacheLimit();
				int period = (int) restrictions.getFullGeocacheLimitPeriod();
				
				String periodString = "";
				if (period < 60) {
					periodString = mContext.getResources().getQuantityString(R.plurals.plurals_minute, period, period);
				} else {
					period = period / 60;
					periodString = mContext.getResources().getQuantityString(R.plurals.plurals_hour, period, period);
				}
				
				String renewTime = DateFormat.getTimeFormat(mContext).format(restrictions.getRenewFullGeocacheLimit());
				
				String cacheString = mContext.getResources().getQuantityString(R.plurals.plurals_cache, cachesPerPeriod, cachesPerPeriod);
				String errorText = mContext.getString(resText, cacheString, periodString, renewTime);
				
				Log.d("ddddd", errorText);
				
				return ErrorActivity.createErrorIntent(mContext, resTitle, 0, errorText, false, null);
				
			case NumberOfCallsExceded: // 140: too many method calls per minute
				return ErrorActivity.createErrorIntent(mContext, R.string.method_quota_exceeded_title, R.string.method_quota_exceeded_message, null, false, null);

			default:
				return null;
		}
	}
}
