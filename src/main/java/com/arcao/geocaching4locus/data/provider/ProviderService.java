package com.arcao.geocaching4locus.data.provider;

import com.arcao.geocaching4locus.data.provider.callback.CallbackListener;
import com.arcao.geocaching4locus.util.Coordinates;

import locus.api.objects.extra.Waypoint;

/**
 * Created by msloup on 4.11.2014.
 */
public interface ProviderService {
	public boolean isAvailable();
	public void retrieveGeocache(String gcCode, CallbackListener listener) throws ProviderException;
	public void retrieveGeocaches(Waypoint[] originalWaypoints, CallbackListener listener) throws ProviderException;
	public void searchNearest(Coordinates coordinates, CallbackListener listener) throws ProviderException;
	public void searchLiveMap(Coordinates topLeft, Coordinates bottomRight, CallbackListener listener) throws ProviderException;
}
