package com.arcao.geocaching4locus.authentication;

import java.util.Arrays;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;

public class AccountAuthenticator extends AbstractAccountAuthenticator {
	private static final String TAG = "G4L|AccountAuthenicator";
	
	public static final String ACCOUNT_TYPE = AuthenticatorHelper.ACCOUNT_TYPE;
	
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
		} catch (NetworkException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new NetworkErrorException(e);
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
}
