package com.arcao.geocaching4locus.task;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthExpectationFailedException;

import org.apache.http.impl.cookie.DateUtils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.UserProfile;
import com.arcao.geocaching.api.data.apilimits.ApiLimits;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.util.DeviceInfoFactory;
import com.arcao.geocaching4locus.util.UserTask;

public class OAuthLoginTask extends UserTask<String, Void, String[]> {
	private static final String TAG = OAuthLoginTask.class.getName();

	public interface OAuthLoginTaskListener {
		void onLoginUrlAvailable(String url);
		void onOAuthTaskFinished(String userName, String token);
		void onTaskError(Intent errorIntent);
	}

	private WeakReference<OAuthLoginTaskListener> oAuthLoginTaskListenerRef;

	public void setOAuthLoginTaskListener(OAuthLoginTaskListener oAuthLoginTaskListener) {
		this.oAuthLoginTaskListenerRef = new WeakReference<OAuthLoginTaskListener>(oAuthLoginTaskListener);
	}

	@Override
	protected String[] doInBackground(String... params) throws Exception {
		OAuthConsumer consumer = Geocaching4LocusApplication.getOAuthConsumer();
		OAuthProvider provider = Geocaching4LocusApplication.getOAuthProvider();

		// we use server time for OAuth timestamp because device can have wrong timezone or time
		String timestamp = Long.toString(getServerDate(AppConstants.GEOCACHING_WEBSITE_URL).getTime() / 1000);

		try {
			if (params.length == 0) {
				String authUrl = provider.retrieveRequestToken(consumer, AppConstants.OAUTH_CALLBACK_URL, OAuth.OAUTH_TIMESTAMP, timestamp);
				Geocaching4LocusApplication.storeRequestTokens(consumer);
				return new String[] { authUrl };
			} else {
				Geocaching4LocusApplication.loadRequestTokensIfNecessary(consumer);
				provider.retrieveAccessToken(consumer, params[0], OAuth.OAUTH_TIMESTAMP, timestamp);

				// get account name
				GeocachingApi api = LiveGeocachingApiFactory.create();
				api.openSession(consumer.getToken());

				UserProfile userProfile = api.getYourUserProfile(false, false, false, false, false, false, DeviceInfoFactory.create());
				ApiLimits apiLimits = api.getApiLimits();

				// update member type and restrictions
				Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().updateMemberType(userProfile.getUser().getMemberType());
				Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions().updateLimits(apiLimits);

				return new String[] {
						userProfile.getUser().getUserName(),
						api.getSession()
				};
			}
		} catch (OAuthExpectationFailedException e) {
			if (provider.getResponseParameters().containsKey(AppConstants.OAUTH_ERROR_MESSAGE_PARAMETER)) {
				throw new OAuthExpectationFailedException("Request token or token secret not set in server reply. "
						+ provider.getResponseParameters().getFirst(AppConstants.OAUTH_ERROR_MESSAGE_PARAMETER));
			}

			throw e;
		}
	}

	@Override
	protected void onPostExecute(String[] result) {
		OAuthLoginTaskListener listener = oAuthLoginTaskListenerRef.get();

		if (result.length == 1) {

			if (listener != null) {
				listener.onLoginUrlAvailable(result[0]);
			}
		} else if (result.length == 2) {
			if (listener != null) {
				listener.onOAuthTaskFinished(result[0], result[1]);
			}
		}
	}

	@Override
	protected void onException(Throwable t) {
		super.onException(t);

		if (isCancelled())
			return;

		Log.e(TAG, t.getMessage(), t);

		Context mContext = Geocaching4LocusApplication.getAppContext();

		Intent intent = new ExceptionHandler(mContext).handle(t);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);

		OAuthLoginTaskListener listener = oAuthLoginTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskError(intent);
		}
	}

	private static Date getServerDate(String url) {
		HttpURLConnection c = null;

		try {
			Log.i(TAG, "Getting server time from url: " + url);
			c = (HttpURLConnection) new URL(url).openConnection();
			c.setRequestMethod("HEAD");
			c.setDoInput(false);
			c.setDoOutput(false);
			c.connect();
			if (c.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String date = c.getHeaderField("Date");
				if (date != null) {
					Log.i(TAG, "We got time: " + date);
					return DateUtils.parseDate(date);
				}
			}
		} catch (Exception e) {
			new NetworkException(e.getMessage(), e);
		} finally {
			if (c != null)
				c.disconnect();
		}

		Log.e(TAG, "No Date header found in a response, used device time instead.");
		return new Date();
	}
}
