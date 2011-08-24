package geocaching.api.data.type;

public enum LogType {
	Unknown("Unknown", -1, 0),
	FoundIt("Found it", 0, 2),
	DidntFindIt("Didn't find it", 1, 3),
	WriteNote("Write note", 2, 4),
	NeedsMaintenance("Needs Maintenance", 3, 45),
	OwnerMaintenance("Owner Maintenance", 4, 46),
	PublishListing("Publish Listing", 5, 24),
	EnableListing("Enable Listing", 6, 23),
	TemporarilyDisableListing("Temporarily Disable Listing", 7, 22),
	UpdateCoordinates("Update Coordinates", 8, 47),
	Announcement("Announcement", 9, 74),
	WillAttend("Will Attend", 10, 9),
	Attended("Attended", 11, 10),
	PostReviewerNote("Post Reviewer Note", 12, 68),
	NeedsArchived("Needs Archived", 13, 7),
	WebcamPhotoTaken("Webcam Photo Taken", 14, 11),
	RetractListing("Retract Listing", 15, 25);

	private String friendlyName;
	private int id;
	private int groundSpeakId;

	private LogType(String friendlyName, int id, int groundSpeakId) {
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

	public static LogType parseLogType(String log) {
		for (LogType type : values()) {
			if (type.toString().equals(log))
				return type;
		}

		return Unknown;
	}
	
	public static LogType parseLogTypeByGroundSpeakId(int id) {
		for (LogType type : values()) {
			if (type.getGroundSpeakId() == id)
				return type;
		}

		return Unknown;
	}
}
