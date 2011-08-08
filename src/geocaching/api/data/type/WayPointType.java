package geocaching.api.data.type;

public enum WayPointType {
	QuestionToAnswer("Question to Answer", "puzzle.jpg"),
	FinalLocation("Final Location", "flag.jpg"),
	ParkingArea("Parking Area", "pkg.jpg"),
	Trailhead("Trailhead", "trailhead.jpg"),
	StagesOfAMulticache("Stages of a Multicache", "stage.jpg"),
	ReferencePoint("Reference Point", "waypoint.jpg");

	private String friendlyName;
	private String iconName;

	private WayPointType(String friendlyName, String iconName) {
		this.friendlyName = friendlyName;
		this.iconName = iconName;
	}

	@Override
	public String toString() {
		return friendlyName;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public String getId() {
		return friendlyName;
	}

	public String getIconName() {
		return iconName;
	}

	public static WayPointType parseWayPointType(String waypointName) {
		for (WayPointType type : values()) {
			if (type.toString().equals(waypointName))
				return type;
		}

		return ReferencePoint;
	}
}
