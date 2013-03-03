package com.arcao.geocaching.api.impl;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;

public class LiveGeocachingApiFactory {
	public static LiveGeocachingApi create() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(Geocaching4LocusApplication.getAppContext());
		
		if (pref.getBoolean(PrefConstants.USE_COMPRESSION, false)) {
			return new LiveGeocachingApi(new ProxyConfiguration(Geocaching4LocusApplication.getGeocachingApiConfiguration()));
		} else {
			return new LiveGeocachingApi(Geocaching4LocusApplication.getGeocachingApiConfiguration());
		}
		
	}
	
	private static class ProxyConfiguration implements GeocachingApiConfiguration {
		private static final String STAGGING ="stagging";
		
		private String entryPointUrl;
		
		public ProxyConfiguration(GeocachingApiConfiguration configuration) {
			if (configuration.getApiServiceEntryPointUrl().contains(STAGGING)) {
				entryPointUrl = AppConstants.STAGGING_COMPRESSION_PROXY_SERVICE_URL;
			} else {
				entryPointUrl = AppConstants.COMPRESSION_PROXY_SERVICE_URL;
			}
		}
		
		@Override
		public String getApiServiceEntryPointUrl() {
			return entryPointUrl;
		}
		
	}
}
