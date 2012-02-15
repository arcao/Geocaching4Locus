package geocaching.api.data;

import geocaching.api.data.type.AttributeType;
import geocaching.api.data.type.CacheType;
import geocaching.api.data.type.ContainerType;
import geocaching.api.data.type.WayPointType;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import menion.android.locus.addon.publiclib.geoData.Point;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingAttributes;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingData;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataWaypoint;

public class Geocache extends SimpleGeocache {	
	private final String shortDescription;
	private final String longDescription;
	private final String hint;
	private final List<CacheLog> cacheLogs;
	private final List<TravelBug> travelBugs;
	private final List<Waypoint> waypoints;
	private final List<AttributeType> attributes;
	private final List<UserWaypoint> userWaypoints;

	public Geocache(String geoCode, String name, double longitude,
			double latitude, CacheType cacheType, float difficultyRating,
			float terrainRating, String authorGuid, String authorName,
			boolean available, boolean archived, boolean premiumListing,
			String countryName, String stateName, Date created,
			String contactName, ContainerType containerType,
			int trackableCount, boolean found, String shortDescription,
			String longDescrition, String hint, List<CacheLog> cacheLogs,
			List<TravelBug> travelBugs, List<Waypoint> waypoints, List<AttributeType> attributes, List<UserWaypoint> userWaypoints) {
		super(geoCode, name, longitude, latitude, cacheType, difficultyRating,
				terrainRating, authorGuid, authorName, available, archived,
				premiumListing, countryName, stateName, created, contactName,
				containerType, trackableCount, found);
		this.shortDescription = shortDescription;
		this.longDescription = longDescrition;
		this.hint = hint;
		this.cacheLogs = cacheLogs;
		this.travelBugs = travelBugs;
		this.waypoints = waypoints;
		this.attributes = attributes;
		this.userWaypoints = userWaypoints;
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

	public List<Waypoint> getWaypoints() {
		return waypoints;
	}
	
	public List<AttributeType> getAttributes() {
		return attributes;
	}
	
	public List<UserWaypoint> getUserWaypoints() {
		return userWaypoints;
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

		for (Waypoint wayPoint : waypoints) {
			d.waypoints.add(wayPoint.toPointGeocachingDataWaypoint());
		}
		
		for (AttributeType attribute : attributes) {
			d.attributes.add(new PointGeocachingAttributes(attribute.getId(), attribute.isOn()));
		}
		
		int index = 0;
		for (UserWaypoint userWaypoint : userWaypoints) {
			PointGeocachingDataWaypoint w = new PointGeocachingDataWaypoint();
			
			w.type = WayPointType.FinalLocation.getId();
			w.typeImagePath = WayPointType.FinalLocation.getIconName();
			w.lat = userWaypoint.getLatitude();
			w.lon = userWaypoint.getLongitude();
			w.name = String.format("%s %d", WayPointType.FinalLocation.getFriendlyName(), index + 1);
			w.description = userWaypoint.getDescription();
			w.code = createUserWaypointCode(userWaypoint.getCacheCode(), index);
			d.waypoints.add(w);
			
			index++;
		}

		return p;
	}
	
	protected String createUserWaypointCode(String cacheCode, int index) {
		int base = Integer.parseInt("U1", 36);
		String value = Integer.toString(base + index, 36);
		
		return value.substring(value.length() - 2, value.length()) + cacheCode.substring(2);
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
			} catch (Exception e) {
			}
			sb.append("; ");
		}
		return sb.toString();
	}

}
