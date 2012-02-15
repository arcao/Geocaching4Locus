package geocaching.api.impl.live_geocaching_api.filter;

import google.gson.stream.JsonWriter;

import java.io.IOException;

public class PointRadiusFilter implements Filter {
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
	public boolean isValid() {
		return true;
	}
	
	@Override
	public void writeJson(JsonWriter w) throws IOException {
		w.name(NAME);
		w.beginObject();
		
		w.name("DistanceInMeters").value(distanceInMeters);
		
		w.name("Point");
		w.beginObject();
		w.name("Latitude").value(latitude);
		w.name("Longitude").value(longitude);
		w.endObject();
		
		w.endObject();
	}

	@Override
	public String getName() {
		return NAME;
	}
}
