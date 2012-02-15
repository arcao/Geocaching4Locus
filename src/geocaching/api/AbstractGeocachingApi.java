package geocaching.api;

import geocaching.api.data.CacheLog;
import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.TravelBug;
import geocaching.api.data.Waypoint;
import geocaching.api.data.type.CacheType;
import geocaching.api.exception.GeocachingApiException;

import java.util.List;

public abstract class AbstractGeocachingApi {
	protected String session;
	
	public String getSession() {
		return session;
	}
	
	public void openSession(String session) throws GeocachingApiException {
		this.session = session;
	}
	
	public abstract void openSession(String userName, String password) throws GeocachingApiException;	
	public abstract void closeSession();
	public abstract boolean isSessionValid();
	public abstract List<SimpleGeocache> getCachesByCoordinates(double latitude, double longitude, int startPosition, int endPosition, float radiusMiles, CacheType[] cacheTypes) throws GeocachingApiException;
	
	public List<SimpleGeocache> getCachesByCoordinates(double latitude, double longitude, int startPosition, int endPosition, float radiusMiles) throws GeocachingApiException {
		return getCachesByCoordinates(latitude, longitude, startPosition, endPosition, radiusMiles, null);
	}
	
	public abstract SimpleGeocache getCacheSimple(String cacheCode) throws GeocachingApiException;
	public abstract Geocache getCache(String cacheCode) throws GeocachingApiException;
	public abstract List<Waypoint> getWayPointsByCache(String cacheCode) throws GeocachingApiException;
	public abstract TravelBug getTravelBug(String travelBugCode) throws GeocachingApiException;
	public abstract List<TravelBug> getTravelBugsByCache(String cacheCode) throws GeocachingApiException;
	public abstract List<CacheLog> getCacheLogs(String cacheCode, int startPosition, int endPosition) throws GeocachingApiException;
	
	
}
