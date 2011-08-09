package geocaching.api.impl.live_geocaching_api.filter;

import geocaching.api.data.type.CacheType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeocacheTypeFilter implements CacheFilter {
	private static final String NAME = "GeocacheType";

	protected CacheType[] cacheTypes;
	
	public GeocacheTypeFilter(CacheType[] cacheTypes) {
		this.cacheTypes = cacheTypes;
	}
	
	@Override
	public JSONObject toJson() throws JSONException {
		if (cacheTypes.length == 0)
			return null;
		
		// convert to groundspeak cache type ids
		JSONArray jsonArray = new JSONArray();
		for (CacheType cacheType : cacheTypes) {
			jsonArray.put(cacheType.getGroundSpeakId());
		}
			
		return new JSONObject().put("GeocacheTypeIds", jsonArray);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
