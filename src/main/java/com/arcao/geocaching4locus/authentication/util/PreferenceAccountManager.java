package com.arcao.geocaching4locus.authentication.util;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.arcao.geocaching.api.data.type.MemberType;
import com.arcao.geocaching4locus.authentication.LoginActivity;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.github.scribejava.core.model.OAuth1RequestToken;

public class PreferenceAccountManager implements AccountManager {
	private final SharedPreferences mPrefs;
	private final Context mContext;
	private final AccountRestrictions restrictions;

	public PreferenceAccountManager(Context context) {
		// Do not store username, password and hash in default shared preferences
		// PreferencesBackupAgent backup default shared preferences to Google Backup Service
		mContext = context.getApplicationContext();

		mPrefs = mContext.getSharedPreferences(PrefConstants.ACCOUNT_STORAGE_NAME, Context.MODE_PRIVATE);

		restrictions = new AccountRestrictions(mContext);

		upgradeStorage();
	}

	@Override
	@NonNull
	public AccountRestrictions getRestrictions() {
		return restrictions;
	}

	@Override
	@Nullable
	public String getOAuthToken() {
		if (!hasAccount())
			return null;

		return mPrefs.getString(PrefConstants.SESSION, null);
	}

	@Override
	@Nullable
	public Account getAccount() {
		String userName = mPrefs.getString(PrefConstants.USERNAME, null);

		if (userName == null)
			return null;

		return new Account(userName, ACCOUNT_TYPE);
	}

	@NonNull
	@Override
	public Account createAccount(@NonNull String userName) {
		return new Account(userName, ACCOUNT_TYPE);
	}

	@Override
	public void addAccount(@NonNull Account account) {
		if (hasAccount())
			removeAccount();

		mPrefs.edit()
			.putString(PrefConstants.USERNAME, account.name)
			.remove(PrefConstants.SESSION)
			.apply();
	}

	@Override
	public void setOAuthToken(@Nullable String authToken) {
		if (!hasAccount())
			return;

		Editor editor = mPrefs.edit();
		if (authToken != null) {
			editor.putString(PrefConstants.SESSION, authToken);
		} else {
			editor.remove(PrefConstants.SESSION);
		}
		editor.apply();
	}

	@Override
	public boolean hasAccount() {
		return mPrefs.getString(PrefConstants.USERNAME, null) != null;
	}

	@Override
	public void removeAccount() {
		mPrefs.edit()
			.remove(PrefConstants.USERNAME)
			.remove(PrefConstants.PASSWORD)
			.remove(PrefConstants.SESSION)
			.apply();

		restrictions.remove();
	}

	@Override
	public void invalidateOAuthToken() {
		mPrefs.edit()
			.remove(PrefConstants.SESSION)
			.apply();
	}

	private void upgradeStorage() {
		// remove username, password and session from old storage
		SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(mContext);

		int prefVersion = mPrefs.getInt(PrefConstants.PREF_VERSION, 0);

		if (prefVersion < 1) {
			// remove user name, password and session from old storage
			defaultPref.edit()
					.remove(PrefConstants.USERNAME)
					.remove(PrefConstants.PASSWORD)
					.remove(PrefConstants.SESSION)
					.apply();

			// remove old accounts with unset member type property
			removeAccount();
		}
		if (prefVersion < 2) {
			// apply restrictions for basic membership
			if (hasAccount() && !restrictions.isPremiumMember())
				restrictions.updateMemberType(MemberType.Basic);
		}

		// update pref_version to latest one
		if (prefVersion != PrefConstants.CURRENT_PREF_VERSION)
			mPrefs.edit().putInt(PrefConstants.PREF_VERSION, PrefConstants.CURRENT_PREF_VERSION).apply();
	}

	@Override
	public boolean requestSignOn(@NonNull Activity activity, int requestCode) {
		if (hasAccount())
			return false;

		activity.startActivityForResult(LoginActivity.createIntent(activity), requestCode);
		return true;
	}

	@Override
	public void setOAuthRequestToken(@Nullable OAuth1RequestToken token) {
		if (token == null || token.isEmpty())
			return;

		mPrefs.edit()
				.putString(PrefConstants.OAUTH_TOKEN, token.getToken())
				.putString(PrefConstants.OAUTH_TOKEN_SECRET, token.getTokenSecret())
				.putBoolean(PrefConstants.OAUTH_CALLBACK_CONFIRMED, token.isOauthCallbackConfirmed())
				.apply();
	}

	@Override
	@NonNull
	public OAuth1RequestToken getOAuthRequestToken() {
		return new OAuth1RequestToken(
				mPrefs.getString(PrefConstants.OAUTH_TOKEN, ""),
				mPrefs.getString(PrefConstants.OAUTH_TOKEN_SECRET, ""),
				mPrefs.getBoolean(PrefConstants.OAUTH_CALLBACK_CONFIRMED, false),
				null
		);
	}

	@Override
	public void deleteOAuthRequestToken() {
		mPrefs.edit()
				.remove(PrefConstants.OAUTH_TOKEN)
				.remove(PrefConstants.OAUTH_TOKEN_SECRET)
				.remove(PrefConstants.OAUTH_CALLBACK_CONFIRMED)
				.apply();
	}
}
