package geocaching.api.impl.live_geocaching_api.filter;

import geocaching.api.data.type.CacheType;
import google.gson.stream.JsonWriter;

import java.io.IOException;

public class GeocacheTypeFilter implements CacheFilter {
	private static final String NAME = "GeocacheType";

	protected CacheType[] cacheTypes;
	
	public GeocacheTypeFilter(CacheType[] cacheTypes) {
		this.cacheTypes = cacheTypes;
	}
	
	@Override
	public boolean isValid() {
		if (cacheTypes == null || cacheTypes.length == 0)
			return false;
		
		boolean valid = false;
		for (CacheType cacheType : cacheTypes) {
			if (cacheType != null)
				valid = true;
		}
		
		return valid;
	}
	
	@Override
	public void writeJson(JsonWriter w) throws IOException {
		w.name(NAME);
		w.beginObject();
		w.name("GeocacheTypeIds");
		w.beginArray();
		for (CacheType cacheType : cacheTypes) {
			if (cacheType != null)
				w.value(cacheType.getGroundSpeakId());
		}
		w.endArray();
		w.endObject();
	}

	@Override
	public String getName() {
		return NAME;
	}

}
