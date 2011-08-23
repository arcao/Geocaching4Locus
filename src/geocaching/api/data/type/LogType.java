package geocaching.api.data.type;

public enum LogType {
	Unknown("Unknown", -1),
	FoundIt("Found it", 0),
	DidntFindIt("Didn't find it", 1),
	WriteNote("Write note", 2),
	NeedsMaintenance("Needs Maintenance", 3),
	OwnerMaintenance("Owner Maintenance", 4),
	PublishListing("Publish Listing", 5),
	EnableListing("Enable Listing", 6),
	TemporarilyDisableListing("Temporarily Disable Listing", 7),
	UpdateCoordinates("Update Coordinates", 8),
	Announcement("Announcement", 9),
	WillAttend("Will Attend", 10),
	Attended("Attended", 11),
	PostReviewerNote("Post Reviewer Note", 12),
	NeedsArchived("Needs Archived", 13),
	WebcamPhotoTaken("Webcam Photo Taken", 14),
	RetractListing("Retract Listing", 15);

	private String friendlyName;
	private int id;

	private LogType(String friendlyName, int id) {
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

	public static LogType parseLogType(String log) {
		for (LogType type : values()) {
			if (type.toString().equals(log))
				return type;
		}

		return WriteNote;
	}
}
