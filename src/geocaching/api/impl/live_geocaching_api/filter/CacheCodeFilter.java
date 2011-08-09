package geocaching.api.impl.live_geocaching_api.filter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CacheCodeFilter implements CacheFilter {
	private static final String NAME = "CacheCode";
	
	protected String[] caches;
	
	public CacheCodeFilter(String... caches) {
		this.caches = caches;
	}
	
	public String[] getCaches() {
		return caches;
	}
	
	@Override
	public JSONObject toJson() throws JSONException {
		JSONArray jsonArray = new JSONArray();
		
		for(String cache : caches) {
			jsonArray.put(cache);
		}
		
		return new JSONObject().put("CacheCodes", jsonArray);
	}

	@Override
	public String getName() {
		return NAME;
	}

}
