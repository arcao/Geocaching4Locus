package com.arcao.geocaching4locus.data.provider;

import com.arcao.geocaching4locus.util.Coordinates;
import locus.api.objects.extra.Waypoint;

/**
 * Created by msloup on 4.11.2014.
 */
public interface ProviderService {
	public void retrieveGeocache(String gcCode, CallbackListener listener) throws ProviderException;
	public void retrieveGeocaches(Waypoint[] originalWaypoints, CallbackListener listener) throws ProviderException;
	public void searchNearest(Coordinates coordinates, CallbackListener listener) throws ProviderException;
	public void searchLiveMap(Coordinates topLeft, Coordinates bottomRight, CallbackListener listener) throws ProviderException;

	public interface CallbackListener {
		public Callback onCallback(Callback data);
		public void onDataReady(Waypoint[] waypoints);
	}

	public interface Callback {

	}
}
