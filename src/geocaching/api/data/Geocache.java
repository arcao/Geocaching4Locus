package geocaching.api.data;

import geocaching.api.data.type.CacheType;
import geocaching.api.data.type.ContainerType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import menion.android.locus.addon.publiclib.geoData.Point;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingData;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataLog;

public class Geocache extends SimpleGeocache {
	private final String shortDescription;
	private final String longDescription;
	private final String hint;
	private final List<CacheLog> cacheLogs;
	private final List<TravelBug> travelBugs;
	private final List<WayPoint> wayPoints;

	public Geocache(String geoCode, String name, double longitude,
			double latitude, CacheType cacheType, float difficultyRating,
			float terrainRating, String authorGuid, String authorName,
			boolean available, boolean archived, boolean premiumListing,
			String countryName, String stateName, Date created,
			String contactName, ContainerType containerType,
			int trackableCount, boolean found, String shortDescription, 
			String longDescrition, String hint, List<CacheLog> cacheLogs, 
			List<TravelBug> travelBugs, List<WayPoint> wayPoints) {
		super(geoCode, name, longitude, latitude, cacheType, difficultyRating,
				terrainRating, authorGuid, authorName, available, archived,
				premiumListing, countryName, stateName, created, contactName,
				containerType, trackableCount, found);
		this.shortDescription = shortDescription;
		this.longDescription = longDescrition;
		this.hint = hint;
		this.cacheLogs = cacheLogs;
		this.travelBugs = travelBugs;
		this.wayPoints = wayPoints;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public String getHint() {
		return hint;
	}

	public List<CacheLog> getCacheLogs() {
		return cacheLogs;
	}

	public List<TravelBug> getTravelBugs() {
		return travelBugs;
	}

	public List<WayPoint> getWayPoints() {
		return wayPoints;
	}
	
	@Override
	public Point toPoint() {
		Point p = super.toPoint();
		PointGeocachingData d = p.getGeocachingData();
		
		d.shortDescription = shortDescription;
		d.longDescription = longDescription;
		d.encodedHints = hint;

		for (CacheLog log : cacheLogs) {
			d.logs.add(log.toPointGeocachingDataLog());
		}
		
		for (TravelBug bug : travelBugs) {
			d.travelBugs.add(bug.toPointGeocachingDataTravelBug());
		}
		
		for (WayPoint wayPoint : wayPoints) {
			d.waypoints.add(wayPoint.toPointGeocachingDataWaypoint());
		}
		
		return p;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Method m : getClass().getMethods()) {
			if (!m.getName().startsWith("get") ||
			    m.getParameterTypes().length != 0 ||  
			    void.class.equals(m.getReturnType()))
			    continue;
			
			sb.append(m.getName());
			sb.append(':');
			try {
				sb.append(m.invoke(this, new Object[0]));
			} catch (Exception e) {}
			sb.append("; ");
		}
		return sb.toString();
	}

}
