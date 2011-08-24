package geocaching.api;

import geocaching.api.data.CacheLog;
import geocaching.api.data.Geocache;
import geocaching.api.data.ImageData;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.WayPoint;
import geocaching.api.data.type.CacheType;
import geocaching.api.data.type.LogType;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.impl.live_geocaching_api.filter.CacheCodeFilter;
import geocaching.api.impl.live_geocaching_api.filter.CacheFilter;
import geocaching.api.impl.live_geocaching_api.filter.GeocacheTypeFilter;
import geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractGeocachingApiV2 extends AbstractGeocachingApi {
	protected Set<GeocachingApiProgressListener> listeners = new HashSet<GeocachingApiProgressListener>();
	
	@Override
	@Deprecated
	public List<SimpleGeocache> getCachesByCoordinates(double latitude, double longitude, int startPosition, int endPosition, float radiusMiles,
			CacheType[] cacheTypes) throws GeocachingApiException {
		return searchForGeocachesJSON(true, startPosition, endPosition - startPosition + 1, 5, -1, new CacheFilter[] { 
				new PointRadiusFilter(latitude, longitude, (long) (radiusMiles * 1609L)),
				new GeocacheTypeFilter(cacheTypes)
		});
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
		List<SimpleGeocache> caches = getCaches(new String[] {cacheCode}, false, 0, 1, 5, -1);
		if (caches.size() == 0)
			return null;
		return (Geocache) caches.get(0);
	}
	
	public List<SimpleGeocache> getCaches(String[] cacheCodes, boolean isLite, int startIndex, int maxPerPage, int geocacheLogCount, int trackableLogCount) throws GeocachingApiException {
		return searchForGeocachesJSON(isLite, startIndex, maxPerPage, geocacheLogCount, trackableLogCount, new CacheFilter[] {new CacheCodeFilter(cacheCodes)});
	}

	@Override
	public List<WayPoint> getWayPointsByCache(String cacheCode) throws GeocachingApiException {
		return null;
	}
	
	public abstract List<SimpleGeocache> searchForGeocachesJSON(boolean isLite, int startIndex, int maxPerPage, int geocacheLogCount, int trackableLogCount, CacheFilter[] filters) throws GeocachingApiException;
	
	public abstract CacheLog createFieldNoteAndPublish(String cacheCode, LogType logType, Date date, String note, boolean promoteToLog, ImageData imageData, boolean favoriteThisCache) throws GeocachingApiException;
	
	protected void fireProgressListener(int progress) {
		synchronized(listeners) {
			for (GeocachingApiProgressListener listener : listeners) {
				listener.onProgressUpdate(progress);
			}
		}
	}
	
	public void addProgressListener(GeocachingApiProgressListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public void removeProgressListener(GeocachingApiProgressListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
}
