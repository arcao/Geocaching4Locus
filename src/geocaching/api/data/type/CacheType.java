package geocaching.api.data.type;

public enum CacheType {
	TraditionalCache("Traditional Cache", 0, 2),
	MultiCache("Multi-cache", 1, 3),
	UnknownCache("Unknown Cache", 2, 8),
	VirtualCache("Virtual Cache", 3, 4),
	EarthCache("Earthcache", 4, 137),
	ProjectApeCache("Project APE Cache", 5, 9),
	LetterboxHybrid("Letterbox Hybrid", 6, 5),
	WherigoCache("Wherigo Cache", 7, 1858),
	EventCache("Event Cache", 8, 6),
	MegaEventCache("Mega-Event Cache", 9, 453),
	CacheInTrashOutEvent("Cache In Trash Out Event", 10, 13),
	GpsAdventuresExhibit("GPS Adventures Exhibit", 11, 1304),
	WebcamCache("Webcam Cache", 12, 11),
	LocationlessCache("Locationless (Reverse) Cache", 13, 12);

	private String friendlyName;
	private int id;
	private int groundSpeakId;

	private CacheType(String friendlyName, int id, int groundSpeakId) {
		this.friendlyName = friendlyName;
		this.id = id;
		this.groundSpeakId = groundSpeakId;
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
	
	public int getGroundSpeakId() {
		return groundSpeakId;
	}

	public static CacheType parseCacheType(String cache) {
		for (CacheType type : values()) {
			if (type.toString().equals(cache))
				return type;
		}

		return UnknownCache;
	}
	
	public static CacheType parseCacheTypeByGroundSpeakId(int groundSpeakId) {
		for (CacheType type : values()) {
			if (type.getGroundSpeakId() == groundSpeakId)
				return type;
		}

		return UnknownCache;
	}
	
}
