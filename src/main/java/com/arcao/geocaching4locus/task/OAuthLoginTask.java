package com.arcao.geocaching4locus.task;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.UserProfile;
import com.arcao.geocaching.api.data.apilimits.ApiLimits;
import com.arcao.geocaching.api.oauth.GeocachingOAuthProvider;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.BuildConfig;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
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
		void onLoginUrlAvailable(@NonNull String url);
		void onTaskFinished(@Nullable Intent errorIntent);
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
		AuthenticatorHelper helper = app.getAuthenticatorHelper();
		AccountRestrictions accountRestrictions = helper.getRestrictions();

		if (params.length == 0) {
			Token requestToken = service.getRequestToken();
			helper.setOAuthRequestToken(requestToken);
			String authUrl = service.getAuthorizationUrl(requestToken);
			Timber.i("AuthorizationUrl: " + authUrl);
			return new String[]{authUrl};
		} else {
			Token requestToken = helper.getOAuthRequestToken();
			Token accessToken = service.getAccessToken(requestToken, new Verifier(params[0]));

			// get account name
			GeocachingApi api = GeocachingApiFactory.create();
			api.openSession(accessToken.getToken());

			UserProfile userProfile = api.getYourUserProfile(false, false, false, false, false, false, DeviceInfoFactory.create(mContext));
			ApiLimits apiLimits = api.getApiLimits();

			// add account
			if (helper.hasAccount()) {
				helper.removeAccount();
			}

			Account account = helper.createAccount(userProfile.getUser().getUserName());
			helper.addAccount(account);
			helper.setOAuthToken(api.getSession());
			helper.deleteOAuthRequestToken();

			// update member type and restrictions
			accountRestrictions.updateMemberType(userProfile.getUser().getMemberType());
			accountRestrictions.updateLimits(apiLimits);

			return null;
		}
	}

	@Override
	protected void onPostExecute(String[] result) {
		TaskListener listener = mTaskListenerRef.get();

		if (listener == null)
			return;

		if (result != null && result.length == 1) {
			listener.onLoginUrlAvailable(result[0]);
		}
		else {
			listener.onTaskFinished(null);
		}
	}

	@Override
	protected void onException(Throwable t) {
		super.onException(t);

		if (isCancelled())
			return;

		Timber.e(t, t.getMessage());

		Intent intent = new ExceptionHandler(mContext).handle(t);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);

		TaskListener listener = mTaskListenerRef.get();
		if (listener != null) {
			listener.onTaskFinished(intent);
		}
	}
}
