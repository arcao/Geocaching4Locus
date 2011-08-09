package geocaching.api.data.type;

public enum ContainerType {
	NotChosen("Not chosen", 0, 1),
	Micro("Micro", 1, 2),
	Small("Small", 2, 8),
	Regular("Regular", 3, 3),
	Large("Large", 4, 4),
	Huge("Huge", 5, 5),
	Other("Other", 6, 5);

	private String friendlyName;
	private int id;
	private int groundSpeakId;

	private ContainerType(String friendlyName, int id, int groundSpeakId) {
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

	public static ContainerType parseContainerType(String container) {
		for (ContainerType type : values()) {
			if (type.toString().equals(container))
				return type;
		}

		return Other;
	}
	
	public static ContainerType parseContainerTypeByGroundSpeakId(int groundSpeakId) {
		for (ContainerType type : values()) {
			if (type.getGroundSpeakId() == groundSpeakId)
				return type;
		}

		return Other;
	}
}
