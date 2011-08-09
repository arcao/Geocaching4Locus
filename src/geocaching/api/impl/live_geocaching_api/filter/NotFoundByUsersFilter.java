package geocaching.api.impl.live_geocaching_api.filter;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotFoundByUsersFilter implements CacheFilter {
	private static final String NAME = "NotFoundByUsers";
	
	protected String[] userNames;
	
	public NotFoundByUsersFilter(String... userNames) {
		this.userNames = userNames;
	}
	
	public String[] getUserNames() {
		return userNames;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return new JSONObject().put("UserNames", new JSONArray(Arrays.asList(userNames))); 
	}

	@Override
	public String getName() {
		return NAME;
	}

}
