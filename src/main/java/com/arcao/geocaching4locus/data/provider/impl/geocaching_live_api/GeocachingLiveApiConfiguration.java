package com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api;

import android.content.Context;
import android.content.SharedPreferences;

import com.arcao.geocaching.api.data.type.MemberType;

public class GeocachingLiveApiConfiguration implements GeocachingLiveApiKeys {
	private final SharedPreferences preferences;
	protected final AccountRestrictions restrictions;

	public GeocachingLiveApiConfiguration(Context context) {
		preferences = context.getSharedPreferences(GeocachingLiveApiProvider.PROVIDER_ID, Context.MODE_PRIVATE);
		restrictions = new AccountRestrictions(preferences);
	}

	public AccountRestrictions getRestrictions() {
		return restrictions;
	}

	public boolean isAccountValid() {
		return getUserName() != null && getToken() != null;
	}

	public void removeAccount() {
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove(PREF_USERNAME);
		editor.remove(PREF_TOKEN);
		editor.commit();

		restrictions.remove();
	}

	public void addAccount(String username, String token, MemberType memberType) {
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(PREF_USERNAME, username);
		editor.putString(PREF_TOKEN, token);
		editor.commit();

		restrictions.updateMemberType(memberType);
	}

	public String getUserName() {
		return preferences.getString(PREF_USERNAME, null);
	}

	public String getToken() {
		return preferences.getString(PREF_TOKEN, null);
	}

	public int getGeocacheLogCount() {
		return preferences.getInt(PREF_DOWNLOADING_COUNT_OF_LOGS, 5);
	}
}
