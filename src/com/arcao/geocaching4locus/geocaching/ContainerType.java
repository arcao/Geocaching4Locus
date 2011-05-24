package com.arcao.geocaching4locus.geocaching;

public enum ContainerType {
	NotChosen("Not chosen"),
	Micro("Micro"),
	Small("Small"),
	Regular("Regular"),
	Large("Large"),
	Virtual("Virtual"),
	Other("Other");
	
	private String friendlyName;
	
	private ContainerType(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
	@Override
	public String toString() {
		return friendlyName;
	}

	public static ContainerType parseContainerType(String container) {
		for(ContainerType type : values()) {
			if (type.toString().equals(container))
				return type;
		}
		
		return NotChosen;
	}
}
