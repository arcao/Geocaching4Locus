package com.arcao.geocaching4locus.authentication.helper;

import android.accounts.Account;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.scribe.model.Token;

public interface AuthenticatorHelper {
	String ACCOUNT_TYPE = "com.arcao.geocaching4locus";

	@Nullable
	String getOAuthToken();
	@Nullable
	Account getAccount();
	@NonNull
	Account createAccount(@NonNull String userName);
	void addAccount(@NonNull Account account);
	void setOAuthToken(@Nullable String authToken);
	boolean hasAccount();
	void removeAccount();
	void invalidateOAuthToken();

	@NonNull
	AccountRestrictions getRestrictions();
	boolean requestSignOn(@NonNull Activity activity, int requestCode);

	void setOAuthRequestToken(@Nullable Token token);
	Token getOAuthRequestToken();
	void deleteOAuthRequestToken();
}