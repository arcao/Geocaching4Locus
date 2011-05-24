package com.arcao.geocaching4locus.geocaching;

public enum CacheType {
	EarthCache("Earthcache"),
	EventCache("Event Cache"),
	GpsAdventuresExhibit("GPS Adventures Exhibit"),
	LetterboxHybrid("Letterbox Hybrid"),
	LocationlessCache("Locationless (Reverse) Cache"),
	MultiCache("Multi-cache"),
	ProjectApeCache("Project APE Cache"),
	TraditionalCache("Traditional Cache"),
	UnknownCache("Unknown Cache"),
	VirtualCache("Virtual Cache"),
	WebcamCache("Webcam Cache"),
	WherigoCache("Wherigo Cache");
	
	private String friendlyName;
	
	private CacheType(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
	@Override
	public String toString() {
		return friendlyName;
	}
	
	public static CacheType parseCacheType(String cache) {
		for(CacheType type : values()) {
			if (type.toString().equals(cache))
				return type;
		}
		
		return UnknownCache;
	}
}
