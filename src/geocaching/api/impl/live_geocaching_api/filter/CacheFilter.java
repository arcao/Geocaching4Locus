package geocaching.api.impl.live_geocaching_api.filter;

import google.gson.stream.JsonWriter;

import java.io.IOException;

public interface CacheFilter {
	public abstract void writeJson(JsonWriter w) throws IOException;
	public abstract String getName();
	public abstract boolean isValid();
}
