package com.arcao.geocaching4locus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.arcao.geocaching4locus.constants.PrefConstants;

public class AccountPreference {
	public static Account get(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);		
		
		return new Account(
				prefs.getString(PrefConstants.USERNAME, ""),
				prefs.getString(PrefConstants.PASSWORD, ""),
				prefs.getString(PrefConstants.SESSION, null)
		);
	}
	
	public static void set(Context context, Account account) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		setOrRemoveString(edit, PrefConstants.USERNAME, account.getUserName());
		setOrRemoveString(edit, PrefConstants.PASSWORD, account.getPassword());
		setOrRemoveString(edit, PrefConstants.SESSION, account.getSession());
		edit.commit();
	}
	
	public static void updateSession(Context context, Account account) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		setOrRemoveString(edit, PrefConstants.SESSION, account.getSession());
		edit.commit();
	}
	
	protected static void setOrRemoveString(Editor edit, String name, String value) {
		if (value != null) {
			edit.putString(name, value);
		} else {
			edit.remove(name);
		}
	}
	
}
