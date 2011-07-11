package geocaching.api.data;

import java.lang.reflect.Method;

import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataTravelBug;

public class TravelBug {
	private final String guid;
	private final String name;
	private final String goal;
	private final String description;
	private final String travelBugTypeName;
	private final String travelBugTypeImage;
	private final String ownerUserName;
	private final String currentCacheCode;
	private final String currentHolderUserName;
	private final String trackingNumber;
	private final String lookupCode;
	
	public TravelBug(String guid, String name, String goal, String description,
			String travelBugTypeName, String travelBugTypeImage,
			String ownerUserName, String currentCacheCode,
			String currentHolderUserName, String trackingNumber) {
		this.guid = guid;
		this.name = name;
		this.goal = goal;
		this.description = description;
		this.travelBugTypeName = travelBugTypeName;
		this.travelBugTypeImage = travelBugTypeImage;
		this.ownerUserName = ownerUserName;
		this.currentCacheCode = currentCacheCode;
		this.currentHolderUserName = currentHolderUserName;
		this.trackingNumber = trackingNumber;
		
		lookupCode = "";
	}

	public TravelBug(String lookupCode, String name, String trackingNumber,
			String ownerUserName) {
		this.lookupCode = lookupCode;
		this.name = name;
		this.trackingNumber = trackingNumber;
		this.ownerUserName = ownerUserName;
		
		guid = "";
		goal = "";
		description = "";
		travelBugTypeName = "";
		travelBugTypeImage = "";
		currentCacheCode = "";
		currentHolderUserName = "";
	}

	public TravelBug(String trackingNumber, String name, String currentCacheCode) {
		this.trackingNumber = trackingNumber;
		this.name = name;
		this.currentCacheCode = currentCacheCode;
		
		guid = "";
		goal = "";
		description = "";
		travelBugTypeName = "";
		travelBugTypeImage = "";
		ownerUserName = "";
		currentHolderUserName = "";
		lookupCode = "";
	}

	public String getGuid() {
		return guid;
	}

	public String getName() {
		return name;
	}

	public String getGoal() {
		return goal;
	}

	public String getDescription() {
		return description;
	}

	public String getTravelBugTypeName() {
		return travelBugTypeName;
	}

	public String getTravelBugTypeImage() {
		return travelBugTypeImage;
	}

	public String getOwnerUserName() {
		return ownerUserName;
	}

	public String getCurrentCacheCode() {
		return currentCacheCode;
	}

	public String getCurrentHolderUserName() {
		return currentHolderUserName;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public String getLookupCode() {
		return lookupCode;
	}
	
	public PointGeocachingDataTravelBug toPointGeocachingDataTravelBug() {
		PointGeocachingDataTravelBug p = new PointGeocachingDataTravelBug();
		
		p.details = description;
		p.goal = goal;
		p.imgUrl = travelBugTypeImage;
		p.name = name;
		// p.origin
		p.owner = ownerUserName;
		// p.released
		// p.srcDetails
		
		return p;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Method m : getClass().getMethods()) {
			if (!m.getName().startsWith("get") ||
			    m.getParameterTypes().length != 0 ||  
			    void.class.equals(m.getReturnType()))
			    continue;
			
			sb.append(m.getName());
			sb.append(':');
			try {
				sb.append(m.invoke(this, new Object[0]));
			} catch (Exception e) {}
			sb.append("; ");
		}
		return sb.toString();
	}
}
