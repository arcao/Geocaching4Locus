package geocaching.api.impl.live_geocaching_api.filter;

import org.json.JSONException;
import org.json.JSONObject;

public class PointRadiusFilter implements CacheFilter {
	private static final String FORMAT = "{\"DistanceInMeters\":%d,\"Point\":{\"Latitude\":%f,\"Longitude\":%f}}";
	private static final String NAME = "PointRadius"; 
	
	protected long distanceInMeters;
	protected double latitude;
	protected double longitude;
	
	public PointRadiusFilter(double latitude, double longitude, long distanceInMeters) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.distanceInMeters = distanceInMeters;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public long getDistanceInMeters() {
		return distanceInMeters;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		return new JSONObject(String.format(FORMAT, distanceInMeters, latitude, longitude));
	}

	@Override
	public String getName() {
		return NAME;
	}
}
