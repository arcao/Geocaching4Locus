package com.arcao.geocaching.api.impl;

import com.arcao.geocaching4locus.Geocaching4LocusApplication;

public class LiveGeocachingApiFactory {
	public static LiveGeocachingApi create() {
		return new LiveGeocachingApi(Geocaching4LocusApplication.getGeocachingApiConfiguration());
	}
}
