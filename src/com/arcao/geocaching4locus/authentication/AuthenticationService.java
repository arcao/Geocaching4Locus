package com.arcao.geocaching4locus.authentication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticationService extends Service {
	AccountAuthenticator accountAuthenticator = null;
	
	@Override
	public IBinder onBind(Intent intent) {
		return getAuthenticator().getIBinder();
	}

	private AccountAuthenticator getAuthenticator() {
	  if (accountAuthenticator == null)
	   accountAuthenticator = new AccountAuthenticator(this);
	  return accountAuthenticator;
	 }
}
