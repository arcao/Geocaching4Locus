package com.arcao.geocaching4locus.authenticatation;

import java.io.IOException;
import java.util.Arrays;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.NetworkErrorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class AccountAuthenticator extends AbstractAccountAuthenticator {
	private static final String TAG = "G4L|AccountAuthenicator";
	
	public static final String ACCOUNT_TYPE = "com.arcao.geocaching4locus";
	
	protected final Context mContext;

	public AccountAuthenticator(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,  String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		final AccountManager am = AccountManager.get(mContext);
		
		// only one account is allowed
		if (am.getAccountsByType(ACCOUNT_TYPE).length > 0)
			return null;
		
	  final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
	  intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
	  intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
	  
	  final Bundle bundle = new Bundle();
	  bundle.putParcelable(AccountManager.KEY_INTENT, intent);
	  return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
		Log.i(TAG, "confirmCredentials: " + response);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
		Log.i(TAG, "editProperties: " + response);
		
		throw new UnsupportedOperationException(); 
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		final Bundle result = new Bundle();

		if (!ACCOUNT_TYPE.equals(authTokenType)) {
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
			return result;
		}

		final AccountManager am = AccountManager.get(mContext);
		
		final String password = am.getPassword(account);
		
		GeocachingApi api = new LiveGeocachingApi(AppConstants.CONSUMER_KEY, AppConstants.LICENCE_KEY);

		try {
			if (password != null) {
				api.openSession(account.name, password);
				
				result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
				result.putString(AccountManager.KEY_AUTHTOKEN, api.getSession());
				return result;
			}
		} catch (InvalidCredentialsException e) {
			result.putString(AccountManager.KEY_AUTH_FAILED_MESSAGE, mContext.getString(R.string.error_credentials));
			Log.e(TAG, e.getMessage(), e);
		} catch (GeocachingApiException e) {
			Log.e(TAG, e.getMessage(), e);
			result.putString(AccountManager.KEY_ERROR_MESSAGE, e.getMessage());
			return result;
		}
		
		// Password is missing or incorrect. Start the activity to add the missing
		// data.
		final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
		intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		
		result.putParcelable(AccountManager.KEY_INTENT, intent);
		return result;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		if (ACCOUNT_TYPE.equals(authTokenType)) { 
			return mContext.getString(R.string.app_name);
		}
		
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
		Log.i(TAG, "hasFeatures: " + Arrays.toString(features));
		
		final Bundle result = new Bundle();
    result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
    return result; 
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
		Log.i(TAG, "updateCredentials: " + response);
		
		final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
    intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
    intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
    //intent.putExtra(AuthenticatorActivity.PARAM_CONFIRMCREDENTIALS, false);
    
    final Bundle bundle = new Bundle();
    bundle.putParcelable(AccountManager.KEY_INTENT, intent);
    return bundle; 
	}
	
	// -------------- Helper functions --------------------------
	public static String getAuthToken(Context ctx) throws OperationCanceledException, AuthenticatorException, IOException {
		final AccountManager am = AccountManager.get(ctx);
		
		Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
		
		if (accounts == null || accounts.length == 0) {
			return null;
		}
		
		return am.blockingGetAuthToken(accounts[0], ACCOUNT_TYPE, true);
	}
	
	public static void addAccount(Context ctx, String username, String password, Parcelable response) {
		final AccountAuthenticatorResponse authResponse = (AccountAuthenticatorResponse)response;

		Bundle result = null;
		final Account account = new Account(username, ACCOUNT_TYPE);
		
		final AccountManager am = AccountManager.get(ctx);
		if (am.addAccountExplicitly(account, password, null)) {
			result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
			authResponse.onResult(result);
		}
	}
	
	public static Account getAccount(Context ctx) {
		final AccountManager am = AccountManager.get(ctx);
		
		Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
		
		if (accounts == null || accounts.length == 0) {
			return null;
		}
		
		return accounts[0];
	}
	
	public static void addAccount(Activity activity) throws OperationCanceledException, AuthenticatorException, IOException {
		final AccountManager am = AccountManager.get(activity);
		
		am.addAccount(ACCOUNT_TYPE, ACCOUNT_TYPE, null, null, activity, null, null);
	}
	
	public static boolean hasAccount(Context ctx) {
		final AccountManager am = AccountManager.get(ctx);
		
		Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
		
		return accounts != null && accounts.length > 0;
	}

	public static void removeAccount(Context ctx) {
		final AccountManager am = AccountManager.get(ctx);
		
		final Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
		
		for(Account account : accounts) {
			am.removeAccount(account, null, null);
		}
	}
	
	public static void clearPassword(Context ctx) {
		final AccountManager am = AccountManager.get(ctx);
		
		final Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
		
		for(Account account : accounts) {
			am.clearPassword(account);
		}
	}
	
	public static void invalidateAuthToken(Context ctx) {
		final AccountManager am = AccountManager.get(ctx);
		
		final Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
		for(Account account : accounts) {
			String token = am.peekAuthToken(account, ACCOUNT_TYPE);
			if (token != null) {
				am.invalidateAuthToken(ACCOUNT_TYPE, token);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void convertFromOldStorage(Context ctx) {
		final SharedPreferences prefs = ctx.getSharedPreferences("default", Context.MODE_PRIVATE);
		
		try {
			String username = prefs.getString(PrefConstants.USERNAME, null);
			String password = prefs.getString(PrefConstants.PASSWORD, null);
			
			if (username == null || username.length() == 0 || password == null || password.length() == 0)
				return;

			final AccountManager am = AccountManager.get(ctx);
			
			Account[] accounts = am.getAccountsByType(ACCOUNT_TYPE);
			
			// only one account allowed
			if (accounts != null && accounts.length != 0)
				return;
			
			// create account in account manager
			final Account account = new Account(username, ACCOUNT_TYPE);
			am.addAccountExplicitly(account, password, null);
		} finally {
			// remove username, password and session from old storage
			prefs.edit().remove(PrefConstants.USERNAME).remove(PrefConstants.PASSWORD).remove(PrefConstants.SESSION).commit();
		}
	}
}
