package com.arcao.geocaching4locus.authentication.util;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.arcao.geocaching.api.data.User;
import com.github.scribejava.core.model.OAuth1RequestToken;

public interface AccountManager {
	String ACCOUNT_TYPE = "com.arcao.geocaching4locus";

	@Nullable
	Account getAccount();
	@NonNull
	Account createAccount(@NonNull User user);
	void addAccount(@NonNull Account account);
	void removeAccount();
	boolean isPremium();

	@Nullable
	String getOAuthToken();
	void setOAuthToken(@Nullable String authToken);
	void invalidateOAuthToken();

	boolean isAccountUpdateRequired();
	void updateAccountNextTime();
	void updateAccount(@NonNull Account account);

	@NonNull
	AccountRestrictions getRestrictions();

	boolean requestSignOn(@NonNull Activity activity, int requestCode);

	void setOAuthRequestToken(@Nullable OAuth1RequestToken token);
	OAuth1RequestToken getOAuthRequestToken();
	void deleteOAuthRequestToken();
}