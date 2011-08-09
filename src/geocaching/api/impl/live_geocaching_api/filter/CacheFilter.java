package geocaching.api.impl.live_geocaching_api.filter;

import org.json.JSONException;
import org.json.JSONObject;

public interface CacheFilter {
	public abstract JSONObject toJson() throws JSONException;
	public abstract String getName();
}
