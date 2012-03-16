package com.arcao.geocaching4locus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AccountPreference {
	public static Account get(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);		
		
		return new Account(
				prefs.getString("username", ""),
				prefs.getString("password", ""),
				prefs.getString("session", null)
		);
	}
	
	public static void set(Context context, Account account) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		
		setOrRemoveString(edit, "username", account.getUserName());
		setOrRemoveString(edit, "password", account.getPassword());
		setOrRemoveString(edit, "session", account.getSession());
		edit.commit();
	}
	
	public static void updateSession(Context context, Account account) {
		Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
		setOrRemoveString(edit, "session", account.getSession());
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
