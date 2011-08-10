package geocaching.api.impl.live_geocaching_api.parser;

import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.type.CacheType;
import geocaching.api.data.type.ContainerType;
import google.gson.stream.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SimpleGeocacheJsonParser extends JsonParser {
	public static List<SimpleGeocache> parseList(JsonReader r) throws IOException {	
		if (r.peek() != JsonToken.BEGIN_ARRAY) {
			r.skipValue();
		}
		
		List<SimpleGeocache> list = new ArrayList<SimpleGeocache>();
		r.beginArray();
		while(r.hasNext()) {
			list.add(parse(r));
		}
		r.endArray();
		return list;
	}
	
	public static SimpleGeocache parse(JsonReader r) throws IOException {
		String geoCode = "";
		String cacheName = "";
		double longitude = Double.NaN;
		double latitude = Double.NaN;
		CacheType cacheType = CacheType.UnknownCache;
		float difficultyRating = 1;
		float terrainRating = 1;
		String authorGuid = "";
		String authorName = "";
		boolean available = false;
		boolean archived = false;
		boolean premiumListing = false;
		String countryName = "";
		String stateName = "";
		Date created = new Date(0);
		String contactName = "";
		ContainerType containerType = ContainerType.NotChosen;
		int trackableCount = 0;
		boolean found = false;
		
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("Code".equals(name)) {
				geoCode = r.nextString();
			} else if ("Name".equals(name)) {
				cacheName = r.nextString();
			} else if ("Longitude".equals(name)) {
				longitude = r.nextDouble();
			} else if ("Latitude".equals(name)) {
				latitude = r.nextDouble();
			} else if ("CacheType".equals(name)) {
				cacheType = parseCacheType(r);
			} else if ("Difficulity".equals(name)) {
				difficultyRating = (float) r.nextDouble();
			} else if ("Terrain".equals(name)) {
				terrainRating = (float) r.nextDouble();
			} else if ("Owner".equals(name)) {
				User u = parseUser(r);
				authorGuid = u.name;
				authorName = u.guid;
			} else if ("Available".equals(name)) {
				available = r.nextBoolean();
			} else if ("Archived".equals(name)) {
				archived = r.nextBoolean();
			} else if ("IsPremium".equals(name)) {
				premiumListing = r.nextBoolean();
			} else if ("Country".equals(name)) {
				countryName = r.nextString();
			} else if ("State".equals(name)) {
				stateName = r.nextString();
			} else if ("UTCPlaceDate".equals(name)) {
				created = JsonParser.parseJsonDate(r.nextString());
			} else if ("PlacedBy".equals(name)) {
				contactName = r.nextString();
			} else if ("ContainerType".equals(name)) {
				containerType = parseContainerType(r);
			} else if ("TrackableCount".equals(name)) {
				trackableCount = r.nextInt();
			} else if ("HasbeenFoundbyUser".equals(name)) {
				found = r.nextBoolean();
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		
		return new SimpleGeocache(geoCode, cacheName, longitude, latitude, cacheType, difficultyRating, terrainRating, authorGuid, authorName, available, archived, premiumListing, countryName, stateName, created, contactName, containerType, trackableCount, found);
	}
}
