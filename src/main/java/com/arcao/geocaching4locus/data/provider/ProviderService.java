package com.arcao.geocaching4locus.data.provider;

import com.arcao.geocaching4locus.data.provider.callback.CallbackListener;
import com.arcao.geocaching4locus.data.provider.exception.ProviderException;
import com.arcao.geocaching4locus.util.Coordinates;

import locus.api.objects.extra.Waypoint;

public interface ProviderService {
	boolean isAvailable();
	void retrieveGeocache(String gcCode, CallbackListener listener) throws ProviderException;
	void retrieveGeocaches(Waypoint[] originalWaypoints, CallbackListener listener) throws ProviderException;
	void searchNearest(Coordinates coordinates, CallbackListener listener) throws ProviderException;
	void searchLiveMap(Coordinates topLeft, Coordinates bottomRight, CallbackListener listener) throws ProviderException;
}
