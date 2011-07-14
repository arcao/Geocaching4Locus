package geocaching.api;

import geocaching.api.data.CacheLog;
import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.TravelBug;
import geocaching.api.data.WayPoint;
import geocaching.api.data.type.CacheType;
import geocaching.api.exception.SessionInvalidException;

import java.util.List;

public abstract class AbstractGeocachingApi {
	protected String session;
	
	public String getSession() {
		return session;
	}
	
	public void openSession(String session) throws SessionInvalidException {
		this.session = session;
	}
	
	public abstract void openSession(String userName, String password) throws SessionInvalidException;	
	public abstract void closeSession();
	public abstract boolean isSessionValid();
	public abstract List<SimpleGeocache> getCachesByCoordinates(double latitude, double longitude, int startPosition, int endPosition, float radiusMiles, CacheType[] cacheTypes) throws SessionInvalidException;
	
	public List<SimpleGeocache> getCachesByCoordinates(double latitude, double longitude, int startPosition, int endPosition, float radiusMiles) throws SessionInvalidException {
		return getCachesByCoordinates(latitude, longitude, startPosition, endPosition, radiusMiles, null);
	}
	
	public abstract SimpleGeocache getCacheSimple(String cacheCode) throws SessionInvalidException;
	public abstract Geocache getCache(String cacheCode) throws SessionInvalidException;
	public abstract List<WayPoint> getWayPointsByCache(String cacheCode) throws SessionInvalidException;
	public abstract TravelBug getTravelBug(String travelBugCode) throws SessionInvalidException;
	public abstract List<TravelBug> getTravelBugsByCache(String cacheCode) throws SessionInvalidException;
	public abstract List<CacheLog> getCacheLogs(String cacheCode, int startPosition, int endPosition) throws SessionInvalidException;
	
	
}
