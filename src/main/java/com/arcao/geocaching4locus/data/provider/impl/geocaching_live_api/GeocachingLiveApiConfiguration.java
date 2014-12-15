package com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api;

import android.content.Context;
import android.content.SharedPreferences;

public class GeocachingLiveApiConfiguration {
	private final SharedPreferences preferences;
	protected final AccountRestrictions restrictions;

	public GeocachingLiveApiConfiguration(Context context) {
		preferences = context.getSharedPreferences(GeocachingLiveApiProvider.PROVIDER_ID);
		restrictions = new AccountRestrictions(preferences);
	}

	public AccountRestrictions getRestrictions() {
		return restrictions;
	}
}
