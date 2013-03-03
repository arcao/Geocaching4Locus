package com.arcao.geocaching4locus.exception;

import android.content.Context;
import android.content.Intent;

import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.live_geocaching_api.exception.LiveGeocachingApiException;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.R;
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

			return ErrorActivity.createErrorIntent(mContext, R.string.error, String.format("%s<br>Exception: %s", message, t.getClass().getSimpleName()), false, t);
		}
	}

	protected Intent handleLiveGeocachingApiExceptions(LiveGeocachingApiException t) {
		switch (t.getStatusCode()) {
			/*case CacheLimitExceeded: // 118: user reach the quota limit
				return ErrorActivity.createErrorIntent(mContext, R.string.error_credentials, null, true, null);
			case NumberOfCallsExceded: // 140: 0to many method calls per minute
				return ErrorActivity.createErrorIntent(mContext, R.string.error_credentials, null, true, null);*/
			default:
				return null;
		}
	}
}
