package geocaching.api.impl.live_geocaching_api.parser;

import geocaching.api.data.TravelBug;
import google.gson.stream.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TravelBugJsonParser extends JsonParser {
	public static List<TravelBug> parseList(JsonReader r) throws IOException {
		if (r.peek() != JsonToken.BEGIN_ARRAY) {
			r.skipValue();
		}
		
		List<TravelBug> list = new ArrayList<TravelBug>();
		r.beginArray();
		while(r.hasNext()) {
			list.add(parse(r));
		}
		r.endArray();
		return list;
	}
	
	public static TravelBug parse(JsonReader r) throws IOException {
		String guid = "";
		String travelBugName = "";
		String goal = "";
		String description = "";
		String travelBugTypeName = "";
		String travelBugTypeImage = "";
		String ownerUserName = "";
		String currentCacheCode = "";
		String currentHolderUserName = "";
		String trackingNumber = "";

		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("Id".equals(name)) {
				guid = r.nextString();
			} else if ("Name".equals(name)) {
				travelBugName = r.nextString();
			} else if ("CurrentGoal".equals(name)) {
				goal = r.nextString();
			} else if ("Description".equals(name)) {
				description = r.nextString();
			} else if ("TBTypeName".equals(name)) {
				travelBugTypeName = r.nextString();
			} else if ("IconUrl".equals(name)) {
				travelBugTypeImage = r.nextString();
			} else if ("OriginalOwner".equals(name)) {
				ownerUserName = parseUser(r).name;
			} else if ("CurrentGeocacheCode".equals(name)) {
				currentCacheCode = r.nextString();
			} else if ("CurrentOwner".equals(name)) {
				currentHolderUserName = parseUser(r).name;
			} else if ("Code".equals(name)) {
				trackingNumber = r.nextString();
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		
		return new TravelBug(guid, travelBugName, goal, description, travelBugTypeName, travelBugTypeImage, ownerUserName, currentCacheCode, currentHolderUserName, trackingNumber);
	}
}
