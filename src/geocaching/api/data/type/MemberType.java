package geocaching.api.data.type;

public enum MemberType {
	Guest("Guest", 0),
	Basic("Basic", 1),
	Charter("Charter", 2),
	Premium("Premium", 3);

	protected String friendlyName;
	protected int groundSpeakId;
	
	private MemberType(String friendlyName, int groundSpeakId) {
		this.friendlyName = friendlyName;
		this.groundSpeakId = groundSpeakId;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public int getGroundSpeakId() {
		return groundSpeakId;
	}
	
	public static MemberType parseMemeberTypeByGroundSpeakId(int groundSpeakId) {
		for (MemberType memberType : values()) {
			if (memberType.groundSpeakId == groundSpeakId)
				return memberType;
		}
		
		return Guest;
	}
}
