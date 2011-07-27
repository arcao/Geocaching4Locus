package geocaching.api.data.type;

public enum CacheType {
	TraditionalCache("Traditional Cache", 0),
	MultiCache("Multi-cache", 1),
	UnknownCache("Unknown Cache", 2),
	VirtualCache("Virtual Cache", 3),
	EarthCache("Earthcache", 4),
	ProjectApeCache("Project APE Cache", 5),
	LetterboxHybrid("Letterbox Hybrid", 6),
	WherigoCache("Wherigo Cache", 7),
	EventCache("Event Cache", 8),
	MegaEventCache("Mega-Event Cache", 9),
	CacheInTrashOutEvent("Cache In Trash Out Event", 10),
	GpsAdventuresExhibit("GPS Adventures Exhibit", 11),
	WebcamCache("Webcam Cache", 12),
	LocationlessCache("Locationless (Reverse) Cache", 13);
	
	private String friendlyName;
	private int id;
	
	private CacheType(String friendlyName, int id) {
		this.friendlyName = friendlyName;
		this.id = id;
	}
	
	@Override
	public String toString() {
		return friendlyName;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public int getId() {
		return id;
	}
	
	public static CacheType parseCacheType(String cache) {
		for(CacheType type : values()) {
			if (type.toString().equals(cache))
				return type;
		}
		
		return UnknownCache;
	}
}
