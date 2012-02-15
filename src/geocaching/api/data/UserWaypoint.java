package geocaching.api.data;

import java.util.Date;

public class UserWaypoint {
	protected String cacheCode;
	protected String description;
	protected long id;
	protected double latitude;
	protected double longitude;
	protected Date date;
	protected int userId;
	
	public UserWaypoint(String cacheCode, String description, long id, double latitude, double longitude, Date date, int userId) {
		super();
		this.cacheCode = cacheCode;
		this.description = description;
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.date = date;
		this.userId = userId;
	}
	
	public String getCacheCode() {
		return cacheCode;
	}
	
	public String getDescription() {
		return description;
	}
	
	public long getId() {
		return id;
	}
	
	public double getLatitude() {
		return latitude;
	}
	
	public double getLongitude() {
		return longitude;
	}
	
	public Date getDate() {
		return date;
	}
	
	public int getUserId() {
		return userId;
	}
}
