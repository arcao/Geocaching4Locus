package com.arcao.geocaching4locus.authentication.helper;

import android.accounts.Account;
import android.app.Activity;

public interface AuthenticatorHelper {
	public static final String ACCOUNT_TYPE = "com.arcao.geocaching4locus";

	String getAuthToken();
	Account getAccount();
	void addAccount(Activity activity);
	boolean addAccountExplicitly(Account account, String password);
	void setAuthToken(Account account, final String authTokenType, final String authToken);
	boolean hasAccount();
	void removeAccount();
	void invalidateAuthToken();

	AccountRestrictions getRestrictions();
}