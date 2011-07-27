package geocaching.api;

import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.WayPoint;
import geocaching.api.data.type.CacheType;
import geocaching.api.exception.GeocachingApiException;

import java.util.List;

public abstract class AbstractGeocachingApiV2 extends AbstractGeocachingApi {
	@Override
	@Deprecated
	public List<SimpleGeocache> getCachesByCoordinates(double latitude, double longitude, int startPosition, int endPosition, float radiusMiles,
			CacheType[] cacheTypes) throws GeocachingApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleGeocache getCacheSimple(String cacheCode) throws GeocachingApiException {
		List<SimpleGeocache> caches = getCaches(new String[] {cacheCode}, true, 0, 1, 0, 0);
		if (caches.size() == 0)
			return null;
		return caches.get(0);
	}

	@Override
	public Geocache getCache(String cacheCode) throws GeocachingApiException {
		List<SimpleGeocache> caches = getCaches(new String[] {cacheCode}, false, 0, 1, Integer.MAX_VALUE, Integer.MAX_VALUE);
		if (caches.size() == 0)
			return null;
		return (Geocache) caches.get(0);
	}
	
	public abstract List<SimpleGeocache> getCaches(String[] cacheCodes, boolean isLite, long startIndex, long maxPerPage, long geocacheLogCount, long trackableLogCount) throws GeocachingApiException;

	@Override
	public List<WayPoint> getWayPointsByCache(String cacheCode) throws GeocachingApiException {
		// TODO Auto-generated method stub
		return null;
	}
}
