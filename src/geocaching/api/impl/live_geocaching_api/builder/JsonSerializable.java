package geocaching.api.impl.live_geocaching_api.builder;

import google.gson.stream.JsonWriter;

import java.io.IOException;

public interface JsonSerializable {
	public abstract void writeJson(JsonWriter w) throws IOException;
}
