package com.arcao.geocaching4locus.authentication.helper;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Activity;

import com.arcao.geocaching.api.exception.GeocachingApiException;

public interface AuthenticatorHelper {
	public static final String ACCOUNT_TYPE = "com.arcao.geocaching4locus";

	String getAuthToken() throws OperationCanceledException, GeocachingApiException;
	Account getAccount();
	void addAccount(Activity activity);
	boolean addAccountExplicitly(Account account, String password);
	void setPassword(Account account, String password);
	void setAuthToken(Account account, final String authTokenType, final String authToken);
	boolean hasAccount();
	void removeAccount();
	void invalidateAuthToken();
	void convertFromOldStorage();
}