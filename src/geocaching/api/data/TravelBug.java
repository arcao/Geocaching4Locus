package geocaching.api.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataTravelBug;

public class TravelBug {
	private static final int VERSION = 1;
	
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
	
	private static final String TRACKABLE_URL = "http://www.geocaching.com/track/details.aspx?tracker=%s"; 
	
	public TravelBug(String guid, String name, String goal, String description,
			String travelBugTypeName, String travelBugTypeImage,
			String ownerUserName, String currentCacheCode,
			String currentHolderUserName, String trackingNumber, String lookupCode) {
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
		this.lookupCode = lookupCode;
	}
	
	public TravelBug(String guid, String name, String goal, String description,
			String travelBugTypeName, String travelBugTypeImage,
			String ownerUserName, String currentCacheCode,
			String currentHolderUserName, String trackingNumber) {
		this(guid, name, goal, description, travelBugTypeName, travelBugTypeImage, ownerUserName, currentCacheCode, currentHolderUserName, trackingNumber, "");
	}

	public TravelBug(String lookupCode, String name, String trackingNumber,
			String ownerUserName) {
		
		this("", name, "", "", "", "", ownerUserName, "", "", trackingNumber, lookupCode);
	}

	public TravelBug(String trackingNumber, String name, String currentCacheCode) {
		this("", name, "", "", "", "", "", currentCacheCode, "", trackingNumber, "");
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
		p.srcDetails = String.format(TRACKABLE_URL, trackingNumber);
		
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

	public static TravelBug load(DataInputStream dis) throws IOException {
		if (dis.readInt() != VERSION)
			throw new IOException("Wrong travelbug version.");
		
		return new TravelBug(
				dis.readUTF(),
				dis.readUTF(), 
				dis.readUTF(), 
				dis.readUTF(), 
				dis.readUTF(), 
				dis.readUTF(), 
				dis.readUTF(), 
				dis.readUTF(), 
				dis.readUTF(), 
				dis.readUTF(),
				dis.readUTF()
		);
	}

	public void store(DataOutputStream dos) throws IOException {
		dos.writeInt(VERSION);
		
		dos.writeUTF(guid);
		dos.writeUTF(name);
		dos.writeUTF(goal);
		dos.writeUTF(description);
		dos.writeUTF(travelBugTypeName);
		dos.writeUTF(travelBugTypeImage);
		dos.writeUTF(ownerUserName);
		dos.writeUTF(currentCacheCode);
		dos.writeUTF(currentHolderUserName);
		dos.writeUTF(trackingNumber);
		dos.writeUTF(lookupCode);
	}
}
