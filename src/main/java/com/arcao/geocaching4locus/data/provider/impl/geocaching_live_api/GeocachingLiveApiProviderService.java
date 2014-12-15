package com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api;

import android.content.Context;

import com.arcao.geocaching4locus.data.provider.ProviderException;
import com.arcao.geocaching4locus.data.provider.ProviderService;
import com.arcao.geocaching4locus.data.provider.callback.CallbackListener;
import com.arcao.geocaching4locus.util.Coordinates;

import locus.api.objects.extra.Waypoint;

/**
 * Created by Arcao on 14. 12. 2014.
 */
public class GeocachingLiveApiProviderService implements ProviderService {
	public GeocachingLiveApiProviderService(Context context) {
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public void retrieveGeocache(String gcCode, CallbackListener listener) throws ProviderException {

	}

	@Override
	public void retrieveGeocaches(Waypoint[] originalWaypoints, CallbackListener listener) throws ProviderException {

	}

	@Override
	public void searchNearest(Coordinates coordinates, CallbackListener listener) throws ProviderException {

	}

	@Override
	public void searchLiveMap(Coordinates topLeft, Coordinates bottomRight, CallbackListener listener) throws ProviderException {

	}
}
