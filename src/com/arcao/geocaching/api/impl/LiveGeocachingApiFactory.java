package com.arcao.geocaching.api.impl;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class LiveGeocachingApiFactory {
	public static LiveGeocachingApi create() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(Geocaching4LocusApplication.getAppContext());
		
		if (pref.getBoolean(PrefConstants.USE_COMPRESSION, false)) {
			return new LiveGeocachingApi(AppConstants.COMPRESSION_PROXY_SERVICE_URL);
		} else {
			return new LiveGeocachingApi();
		}
		
	}
}
