package geocaching.api.data.type;

public enum ContainerType {
	NotChosen("Not chosen", 0),
	Micro("Micro", 1),
	Small("Small", 2),
	Regular("Regular", 3),
	Large("Large", 4),
	Huge("Huge", 5),
	Other("Other", 6);
	
	private String friendlyName;
	private int id;
	
	private ContainerType(String friendlyName, int id) {
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

	public static ContainerType parseContainerType(String container) {
		for(ContainerType type : values()) {
			if (type.toString().equals(container))
				return type;
		}
		
		return Other;
	}
}
