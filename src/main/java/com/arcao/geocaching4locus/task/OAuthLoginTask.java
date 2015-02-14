package com.arcao.geocaching4locus.task;

import android.content.Context;
import android.content.Intent;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.UserProfile;
import com.arcao.geocaching.api.data.apilimits.ApiLimits;
import com.arcao.geocaching.api.oauth.GeocachingOAuthProvider;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.BuildConfig;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.util.DeviceInfoFactory;
import com.arcao.geocaching4locus.util.UserTask;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import timber.log.Timber;

import java.lang.ref.WeakReference;

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

	private OAuthService createOAuthService() {
		ServiceBuilder serviceBuilder = new ServiceBuilder()
			.apiKey(BuildConfig.GEOCACHING_API_KEY)
			.apiSecret(BuildConfig.GEOCACHING_API_SECRET)
			.callback(AppConstants.OAUTH_CALLBACK_URL)
			.debug();

		if (BuildConfig.GEOCACHING_API_STAGING) {
			serviceBuilder.provider(GeocachingOAuthProvider.Staging.class);
		} else {
			serviceBuilder.provider(GeocachingOAuthProvider.class);
		}

		return serviceBuilder.build();
	}

	@Override
	protected String[] doInBackground(String... params) throws Exception {
		OAuthService service = createOAuthService();
		App app = App.get(mContext);
		AccountRestrictions accountRestrictions = app.getAuthenticatorHelper().getRestrictions();

		if (params.length == 0) {
			Token requestToken = service.getRequestToken();
			app.storeOAuthToken(requestToken);
			String authUrl = service.getAuthorizationUrl(requestToken);
			Timber.i("AuthorizationUrl: " + authUrl);
			return new String[] { authUrl };
		} else {
			Token requestToken = app.getOAuthToken();
			Token accessToken = service.getAccessToken(requestToken, new Verifier(params[0]));

			// get account name
			GeocachingApi api = GeocachingApiFactory.create();
			api.openSession(accessToken.getToken());

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
}
