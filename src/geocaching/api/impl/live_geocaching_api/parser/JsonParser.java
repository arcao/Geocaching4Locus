package geocaching.api.impl.live_geocaching_api.parser;

import geocaching.api.data.type.AttributeType;
import geocaching.api.data.type.CacheType;
import geocaching.api.data.type.ContainerType;
import google.gson.stream.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class JsonParser {
	private static final String TAG = "Geocaching4Locus|ParserUtil";
	
	public static Date parseJsonDate(String date) {
		Pattern DATE_PATTERN = Pattern.compile("/Date\\((.*)([-+].{4})\\)/");
		
		Matcher m = DATE_PATTERN.matcher(date);
    if (m.matches())
    {
      long time = Long.parseLong(m.group(1));
      long zone = Integer.parseInt(m.group(2)) / 100 * 1000 * 60 * 60;
      return new Date(time + zone);
    }
    
    Log.e(TAG, "parseJsonDate failed: " + date);
    return new Date(0);
	}
	
	protected static CacheType parseCacheType(JsonReader r) throws IOException {
		CacheType cacheType = CacheType.UnknownCache;
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("GeocacheTypeId".equals(name)) {
				cacheType = CacheType.parseCacheTypeByGroundSpeakId(r.nextInt());
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		return cacheType;
	}
	
	protected static ContainerType parseContainerType(JsonReader r) throws IOException {
		ContainerType containerType = ContainerType.NotChosen;
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("ContainerTypeId".equals(name)) {
				containerType = ContainerType.parseContainerTypeByGroundSpeakId(r.nextInt());
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		return containerType;
	}
	
	protected static User parseUser(JsonReader r) throws IOException {
		User u = new User();
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("UserName".equals(name)) {
				u.name = r.nextString();
			} else if ("PublicGuid".equals(name)) {
				u.guid = r.nextString();
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		return u;
	}
	
	protected static AttributeType parseAttributte(JsonReader r) throws IOException {
		int id = 1;
		boolean on = false;
		
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("ID".equals(name)) {
				id = r.nextInt();
			} else if ("PublicGuid".equals(name)) {
				on = r.nextBoolean();
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		
		return AttributeType.parseAttributeTypeByGroundSpeakId(id, on);
	}
	
	protected static List<AttributeType> parseAttributteList(JsonReader r) throws IOException {
		if (r.peek() != JsonToken.BEGIN_ARRAY) {
			r.skipValue();
		}
		
		List<AttributeType> list = new ArrayList<AttributeType>();
		r.beginArray();
		while(r.hasNext()) {
			list.add(parseAttributte(r));
		}
		r.endArray();
		return list;
	}
	
	protected static class User {
		String name;
		String guid;
	}
}
