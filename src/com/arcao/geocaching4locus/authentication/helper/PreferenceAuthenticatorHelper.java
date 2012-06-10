package com.arcao.geocaching4locus.authentication.helper;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.authentication.AuthenticatorActivity;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class PreferenceAuthenticatorHelper implements AuthenticatorHelper {
	protected final SharedPreferences pref;
	protected final Context mContext;
	
	public PreferenceAuthenticatorHelper(Context ctx) {
		// Do not store username, password and hash in default shared preferences
		// Default shared preferences are sent by ACRA in error report
		// Also PreferencesBackupAgent backup default shared preferences to Google Backup Service 
		pref = ctx.getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);
		
		mContext = ctx;
	}
	
	@Override
	public String getAuthToken() throws OperationCanceledException, GeocachingApiException {	
		if (!hasAccount())
			return null;
		
		String token = pref.getString(PrefConstants.SESSION, null);
		if (token != null)
			return token;
		
		String username = pref.getString(PrefConstants.USERNAME, null);
		String password = pref.getString(PrefConstants.PASSWORD, null);
		
		// try to login		
		GeocachingApi api = new LiveGeocachingApi(AppConstants.CONSUMER_KEY, AppConstants.LICENCE_KEY);
		api.openSession(username, password);				
		return api.getSession();
	}
	
	@Override
	public Account getAccount() {
		String username = pref.getString(PrefConstants.USERNAME, null);
		
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
		
		Editor editor = pref.edit();
		editor.putString(PrefConstants.USERNAME, account.name);
		editor.putString(PrefConstants.PASSWORD, password);
		editor.remove(PrefConstants.SESSION);
		editor.commit();

		return true;
	}
	
	@Override
	public void setPassword(Account account, String password) {
		if (!hasAccount())
			return;
		
		Editor editor = pref.edit();
		editor.putString(PrefConstants.PASSWORD, password);
		editor.remove(PrefConstants.SESSION);
		editor.commit();
	}
	
	@Override
	public void setAuthToken(Account account, String authTokenType, String authToken) {
		if (!hasAccount())
			return;
		
		Editor editor = pref.edit();
		if (authToken != null) {
			editor.putString(PrefConstants.SESSION, authToken);
		} else {
			editor.remove(PrefConstants.SESSION);
		}
		editor.commit();
	}
	
	@Override
	public boolean hasAccount() {
		return pref.getString(PrefConstants.USERNAME, null) != null;
	}

	@Override
	public void removeAccount() {
		Editor editor = pref.edit();
		editor.remove(PrefConstants.USERNAME);
		editor.remove(PrefConstants.PASSWORD);
		editor.remove(PrefConstants.SESSION);
		editor.commit();
	}
	
	@Override
	public void invalidateAuthToken() {
		Editor editor = pref.edit();
		editor.remove(PrefConstants.SESSION);
		editor.commit();
	}
	
	@Override
	public void convertFromOldStorage() {
		// remove username, password and session from old storage
		SharedPreferences defaultPref = PreferenceManager.getDefaultSharedPreferences(mContext);
		
		Editor editor = defaultPref.edit();
		editor.remove(PrefConstants.USERNAME);
		editor.remove(PrefConstants.PASSWORD);
		editor.remove(PrefConstants.SESSION);
		editor.commit();
	}
}
