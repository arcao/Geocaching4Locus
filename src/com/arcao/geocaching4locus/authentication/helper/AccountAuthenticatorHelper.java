package com.arcao.geocaching4locus.authentication.helper;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class AccountAuthenticatorHelper implements AuthenticatorHelper {
	protected final Context mContext;
	protected final AccountManager mAccountManager;
	
	public AccountAuthenticatorHelper(Context ctx) {
		mContext = ctx;
		mAccountManager = AccountManager.get(mContext);
	}
	
	@Override
	public String getAuthToken() throws OperationCanceledException, GeocachingApiException {	
		Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		
		if (accounts == null || accounts.length == 0) {
			return null;
		}
		
		try {
			return mAccountManager.blockingGetAuthToken(accounts[0], ACCOUNT_TYPE, true);
		} catch (AuthenticatorException e) {
			throw new GeocachingApiException(e.getMessage(), e);
		} catch (IOException e) {
			throw new NetworkException(e.getMessage(), e);
		}
	}
	
	@Override
	public Account getAccount() {
		Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		
		if (accounts == null || accounts.length == 0) {
			return null;
		}
		
		return accounts[0];
	}
	
	@Override
	public void addAccount(Activity activity) {
		mAccountManager.addAccount(ACCOUNT_TYPE, ACCOUNT_TYPE, null, null, activity, null, null);
	}
	
	@Override
	public boolean addAccountExplicitly(Account account, String password) {
		return mAccountManager.addAccountExplicitly(account, password, null);
	}
	
	@Override
	public void setPassword(Account account, String password) {
		mAccountManager.setPassword(account, password);
	}
	
	@Override
	public void setAuthToken(Account account, String authTokenType, String authToken) {
		mAccountManager.setAuthToken(account, authTokenType, authToken);
	}
	
	@Override
	public boolean hasAccount() {
		Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		
		return accounts != null && accounts.length > 0;
	}

	@Override
	public void removeAccount() {
		final Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		
		for(Account account : accounts) {
			mAccountManager.removeAccount(account, null, null);
		}
	}
	
	@Override
	public void invalidateAuthToken() {
		final Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE);
		for(Account account : accounts) {
			String token = mAccountManager.peekAuthToken(account, ACCOUNT_TYPE);
			if (token != null) {
				mAccountManager.invalidateAuthToken(ACCOUNT_TYPE, token);
			}
		}
	}
	
	@Override
	public void convertFromOldStorage() {
		final SharedPreferences prefs = mContext.getSharedPreferences("ACCOUNT", Context.MODE_PRIVATE);
		
		try {
			String username = prefs.getString(PrefConstants.USERNAME, null);
			String password = prefs.getString(PrefConstants.PASSWORD, null);
			
			if (username == null || username.length() == 0 || password == null || password.length() == 0)
				return;

			// only one account allowed
			if (hasAccount())
				return;
			
			// create account in account manager
			addAccountExplicitly(new Account(username, ACCOUNT_TYPE), password);
		} finally {
			// remove username, password and session from old storage
			prefs.edit().remove(PrefConstants.USERNAME).remove(PrefConstants.PASSWORD).remove(PrefConstants.SESSION).commit();
		}
	}
}
