package geocaching.api.data;

import geocaching.api.data.type.CacheType;

import java.lang.reflect.Method;
import java.util.Date;

import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataWaypoint;

public class WayPoint {
	private String codeName;
	private Date time;
	private String waypointGeoCode;
	private String cacheGeoCode;
	private CacheType cacheType;
	private String name;
	private String iconName;
	private double latitude;
	private double longitude;
	private String note;
	
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

	public PointGeocachingDataWaypoint toPointGeocachingDataWaypoint() {
		PointGeocachingDataWaypoint w = new PointGeocachingDataWaypoint();
		//w.lat = latitude;
		//w.lon = longitude;
		//w.description = note;
		//w.name = name;
		//w.typeImagePath = iconName;
		//w.type = "";
		
		return w;
	}
}
