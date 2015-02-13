package com.arcao.geocaching4locus.task;

import android.content.Context;
import android.content.Intent;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.UserProfile;
import com.arcao.geocaching.api.data.apilimits.ApiLimits;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.util.DeviceInfoFactory;
import com.arcao.geocaching4locus.util.UserTask;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.exception.OAuthExpectationFailedException;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import timber.log.Timber;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class OAuthLoginTask extends UserTask<String, Void, String[]> {
	public interface TaskListener {
		void onLoginUrlAvailable(String url);
		void onOAuthTaskFinished(String userName, String token);
		void onTaskError(Intent errorIntent);
	}

	private final Context mContext;
	private final WeakReference<TaskListener> mTaskListenerRef;

	public OAuthLoginTask(Context context, TaskListener listener) {
		mContext = context.getApplicationContext();
		mTaskListenerRef = new WeakReference<>(listener);
	}

	@Override
	protected String[] doInBackground(String... params) throws Exception {
		OAuthConsumer consumer = LiveGeocachingApiFactory.getOAuthConsumer();
		OAuthProvider provider = LiveGeocachingApiFactory.getOAuthProvider();
		App app = App.get(mContext);
		AccountRestrictions accountRestrictions = app.getAuthenticatorHelper().getRestrictions();

		// we use server time for OAuth timestamp because device can have wrong timezone or time
		String timestamp = Long.toString(getServerDate(AppConstants.GEOCACHING_WEBSITE_URL).getTime() / 1000);

		try {
			if (params.length == 0) {
				String authUrl = provider.retrieveRequestToken(consumer, AppConstants.OAUTH_CALLBACK_URL, OAuth.OAUTH_TIMESTAMP, timestamp);
				app.storeRequestTokens(consumer);
				return new String[] { authUrl };
			} else {
				app.loadRequestTokensIfNecessary(consumer);
				provider.retrieveAccessToken(consumer, params[0], OAuth.OAUTH_TIMESTAMP, timestamp);

				// get account name
				GeocachingApi api = LiveGeocachingApiFactory.getLiveGeocachingApi();
				api.openSession(consumer.getToken());

				UserProfile userProfile = api.getYourUserProfile(false, false, false, false, false, false, DeviceInfoFactory.create(mContext));
				ApiLimits apiLimits = api.getApiLimits();

				// update member type and restrictions
				accountRestrictions.updateMemberType(userProfile.getUser().getMemberType());
				accountRestrictions.updateLimits(apiLimits);

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
		TaskListener listener = mTaskListenerRef.get();

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

		Timber.e(t.getMessage(), t);

		Intent intent = new ExceptionHandler(mContext).handle(t);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskError(intent);
		}
	}

	private static Date getServerDate(String url) throws NetworkException {
		HttpURLConnection c = null;

		try {
			Timber.i("Getting server time from url: " + url);
			c = (HttpURLConnection) new URL(url).openConnection();
			c.setRequestMethod("HEAD");
			c.setDoInput(false);
			c.setDoOutput(false);
			c.connect();
			if (c.getResponseCode() == HttpURLConnection.HTTP_OK) {
				String date = c.getHeaderField("Date");
				if (date != null) {
					Timber.i("We got time: " + date);
					return DateUtils.parseDate(date);
				}
			}
		} catch (IOException | DateParseException e) {
			throw new NetworkException(e.getMessage(), e);
		} finally {
			if (c != null)
				c.disconnect();
		}

		Timber.e("No Date header found in a response, used device time instead.");
		return new Date();
	}
}
