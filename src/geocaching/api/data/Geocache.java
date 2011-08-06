package geocaching.api.data;

import geocaching.api.data.type.CacheType;
import geocaching.api.data.type.ContainerType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import menion.android.locus.addon.publiclib.geoData.Point;
import menion.android.locus.addon.publiclib.geoData.PointGeocachingData;

public class Geocache extends SimpleGeocache {
	private static final int VERSION = 1;
	
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
		this.cacheLogs = cacheLogs != null ? cacheLogs : new ArrayList<CacheLog>();
		this.travelBugs = travelBugs != null ? travelBugs : new ArrayList<TravelBug>();
		this.wayPoints = wayPoints != null ? wayPoints : new ArrayList<WayPoint>();
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
	
	public static Geocache load(DataInputStream dis) throws IOException {
		if (dis.readInt() != VERSION)
			throw new IOException("Wrong item version.");
		
		return new Geocache(
				dis.readUTF(),
				dis.readUTF(),
				dis.readDouble(),
				dis.readDouble(),
				CacheType.parseCacheType(dis.readUTF()),
				dis.readFloat(),
				dis.readFloat(),
				dis.readUTF(),
				dis.readUTF(),
				dis.readBoolean(),
				dis.readBoolean(),
				dis.readBoolean(),
				dis.readUTF(),
				dis.readUTF(),
				new Date(dis.readLong()),
				dis.readUTF(),
				ContainerType.parseContainerType(dis.readUTF()),
				dis.readInt(),
				dis.readBoolean(),
				dis.readUTF(),
				dis.readUTF(),
				dis.readUTF(),
				loadCacheLogs(dis),
				loadTravelBugs(dis),
				loadWayPoints(dis)
		);
	}
	
	protected static List<CacheLog> loadCacheLogs(DataInputStream dis) throws IOException {
		List<CacheLog> list = new ArrayList<CacheLog>();
		
		int count = dis.readInt();
		for (int i = 0; i < count; i++) {
			list.add(CacheLog.load(dis));
		}
		
		return list;
	}
	
	protected static List<TravelBug> loadTravelBugs(DataInputStream dis) throws IOException {
		List<TravelBug> list = new ArrayList<TravelBug>();
		
		int count = dis.readInt();
		for (int i = 0; i < count; i++) {
			list.add(TravelBug.load(dis));
		}
		
		return list;
	}
	
	protected static List<WayPoint> loadWayPoints(DataInputStream dis) throws IOException {
		List<WayPoint> list = new ArrayList<WayPoint>();
		
		int count = dis.readInt();
		for (int i = 0; i < count; i++) {
			list.add(WayPoint.load(dis));
		}
		
		return list;
	}
	
	@Override
	public void store(DataOutputStream dos) throws IOException {
		dos.writeInt(VERSION);
		
		dos.writeUTF(getGeoCode());
		dos.writeUTF(getName());
		dos.writeDouble(getLongitude());
		dos.writeDouble(getLatitude());
		dos.writeUTF(getCacheType().toString());
		dos.writeFloat(getDifficultyRating());
		dos.writeFloat(getTerrainRating());
		dos.writeUTF(getAuthorGuid());
		dos.writeUTF(getAuthorName());
		dos.writeBoolean(isAvailable());
		dos.writeBoolean(isArchived());
		dos.writeBoolean(isPremiumListing());
		dos.writeUTF(getCountryName());
		dos.writeUTF(getStateName());
		dos.writeLong(getCreated().getTime());
		dos.writeUTF(getContactName());
		dos.writeUTF(getContainerType().toString());
		dos.writeInt(getTrackableCount());
		dos.writeBoolean(isFound());
		dos.writeUTF(getShortDescription());
		dos.writeUTF(getLongDescription());
		dos.writeUTF(getHint());
		
		dos.writeInt(cacheLogs.size());
		for(int i = 0; i < cacheLogs.size(); i++)
			cacheLogs.get(i).store(dos);
		
		dos.writeInt(travelBugs.size());
		for(int i = 0; i < travelBugs.size(); i++)
			travelBugs.get(i).store(dos);

		dos.writeInt(wayPoints.size());
		for(int i = 0; i < wayPoints.size(); i++)
			wayPoints.get(i).store(dos);
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
