package com.arcao.geocaching4locus.authentication.helper;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.arcao.geocaching4locus.authentication.AuthenticatorActivity;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class PreferenceAuthenticatorHelper implements AuthenticatorHelper {
	private final SharedPreferences mPrefs;
	private final Context mContext;
	private final AccountRestrictions restrictions;

	public PreferenceAuthenticatorHelper(Context context) {
		// Do not store username, password and hash in default shared preferences
		// Default shared preferences are sent by ACRA in error report
		// Also PreferencesBackupAgent backup default shared preferences to Google Backup Service
		mContext = context.getApplicationContext();

		mPrefs = mContext.getSharedPreferences(PrefConstants.ACCOUNT_STORAGE_NAME, Context.MODE_PRIVATE);

		restrictions = new AccountRestrictions(mContext);

		upgradeStorage();
	}

	@Override
	public AccountRestrictions getRestrictions() {
		return restrictions;
	}

	@Override
	public String getAuthToken() {
		if (!hasAccount())
			return null;

		return mPrefs.getString(PrefConstants.SESSION, null);
	}

	@Override
	public Account getAccount() {
		String username = mPrefs.getString(PrefConstants.USERNAME, null);

		if (username == null)
			return null;

		return new Account(username, ACCOUNT_TYPE);
	}

	@Override
	public void addAccount(Activity activity) {
		activity.startActivity(new Intent(activity, AuthenticatorActivity.class));
	}

	@Override
	public boolean addAccountExplicitly(Account account, String password) {
		if (account == null || hasAccount())
			return false;

		mPrefs.edit()
			.putString(PrefConstants.USERNAME, account.name)
			.remove(PrefConstants.SESSION)
			.apply();

		return true;
	}

	@Override
	public void setAuthToken(Account account, String authTokenType, String authToken) {
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
	public void invalidateAuthToken() {
		mPrefs.edit()
			.remove(PrefConstants.SESSION)
			.apply();
	}

	private void upgradeStorage() {
		// remove username, password and session from old storage
		SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(mContext);

		defaultPref.edit()
			.remove(PrefConstants.USERNAME)
			.remove(PrefConstants.PASSWORD)
			.remove(PrefConstants.SESSION)
			.apply();

		int prefVersion = mPrefs.getInt(PrefConstants.PREF_VERSION, 0);

		// remove account when password is set
		// remove old accounts with not set premium account property
		if (mPrefs.contains(PrefConstants.PASSWORD) || prefVersion < 1) {
			removeAccount();
		}

		mPrefs.edit().putInt(PrefConstants.PREF_VERSION, PrefConstants.CURRENT_PREF_VERSION).apply();
	}

	@Override
	public boolean isLoggedIn(Activity activity, int requestCode) {
		if (hasAccount())
			return true;

		activity.startActivityForResult(AuthenticatorActivity.createIntent(activity), requestCode);
		return false;
	}
}
