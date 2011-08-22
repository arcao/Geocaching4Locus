package geocaching.api.data;

import geocaching.api.data.type.WayPointType;

import java.lang.reflect.Method;
import java.util.Date;

import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataWaypoint;
import android.util.Log;

public class WayPoint {
	private final double latitude;
	private final double longitude;
	private final Date time;
	private final String waypointGeoCode;
	private final String name;
	private final String note;
	private final WayPointType wayPointType;
	private final String iconName;

	public WayPoint(double longitude, double latitude, Date time, String waypointGeoCode, String name, String note, WayPointType wayPointType) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
		this.waypointGeoCode = waypointGeoCode;
		this.name = name;
		this.note = note;
		this.wayPointType = wayPointType;
		this.iconName = wayPointType.getIconName();
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public Date getTime() {
		return time;
	}

	public String getWaypointGeoCode() {
		return waypointGeoCode;
	}

	public String getName() {
		return name;
	}

	public String getNote() {
		return note;
	}

	public WayPointType getWayPointType() {
		return wayPointType;
	}

	public String getIconName() {
		return iconName;
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

	public PointGeocachingDataWaypoint toPointGeocachingDataWaypoint() {
		PointGeocachingDataWaypoint w = new PointGeocachingDataWaypoint();
		w.code = waypointGeoCode;
		w.lat = latitude;
		w.lon = longitude;
		w.description = note;
		w.name = name;
		w.typeImagePath = iconName;
		w.type = wayPointType.getId();
		
		Log.i("Waypoint",  "-----");
		Log.i("Waypoint",  "Code: " + w.code);
		Log.i("Waypoint",  "Desc: " + w.description);
		Log.i("Waypoint",  "Name: " + w.name);
		Log.i("Waypoint",  "Type: " + w.type);
		

		return w;
	}
}
