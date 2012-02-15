package geocaching.api.impl.live_geocaching_api.parser;

import geocaching.api.data.Waypoint;
import geocaching.api.data.type.WayPointType;
import google.gson.stream.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WaypointJsonParser extends JsonParser {
	public static List<Waypoint> parseList(JsonReader r) throws IOException {
		if (r.peek() != JsonToken.BEGIN_ARRAY) {
			r.skipValue();
		}
		
		List<Waypoint> list = new ArrayList<Waypoint>();
		r.beginArray();
		while(r.hasNext()) {
			list.add(parse(r));
		}
		r.endArray();
		return list;
	}
	
	public static Waypoint parse(JsonReader r) throws IOException {
		double longitude = Double.NaN;
		double latitude = Double.NaN;
		Date time = new Date(0);
		String waypointGeoCode = "";
		String waypointName = "";
		String note = "";
		WayPointType wayPointType = WayPointType.ReferencePoint;
		
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("Longitude".equals(name)) {
				longitude = r.nextDouble();
			} else if ("Latitude".equals(name)) {
				latitude = r.nextDouble();
			} else if ("UTCEnteredDate".equals(name)) {
				time = parseJsonDate(r.nextString());
			} else if ("Code".equals(name)) {
				waypointGeoCode = r.nextString();
			} else if ("Name".equals(name)) {
				wayPointType = WayPointType.parseWayPointType(r.nextString());
			} else if ("Description".equals(name)) {
				waypointName = r.nextString();
			} else if ("Comment".equals(name)) {
				note = r.nextString();
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		
		return new Waypoint(longitude, latitude, time, waypointGeoCode, waypointName, note, wayPointType);
	}
}
