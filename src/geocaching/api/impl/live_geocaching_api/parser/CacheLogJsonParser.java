package geocaching.api.impl.live_geocaching_api.parser;

import geocaching.api.data.CacheLog;
import geocaching.api.data.type.LogType;
import google.gson.stream.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CacheLogJsonParser extends JsonParser {
	public static List<CacheLog> parseList(JsonReader r) throws IOException {
		if (r.peek() != JsonToken.BEGIN_ARRAY) {
			r.skipValue();
		}
		
		List<CacheLog> list = new ArrayList<CacheLog>();
		r.beginArray();
		while(r.hasNext()) {
			list.add(parse(r));
		}
		r.endArray();
		return list;
	}
	
	public static CacheLog parse(JsonReader r) throws IOException {
		Date date = new Date(0);
		LogType logType = LogType.Unknown;
		String author = "";
		String text = "";
		
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("UTCCreateDate".equals(name)) {
				date = JsonParser.parseJsonDate(r.nextString());
			} else if ("LogType".equals(name)) {
				logType = parseLogType(r);
			} else if ("Finder".equals(name)) {
				author = parseUser(r).name;
			} else if ("LogText".equals(name)) {
				text = r.nextString();
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		
		return new CacheLog(date, logType, author, text);
	}

	protected static LogType parseLogType(JsonReader r) throws IOException {
		LogType logType = LogType.Unknown;
		
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("WptLogTypeName".equals(name)) {
				logType = LogType.parseLogType(r.nextString());
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		return logType;
	}
}
