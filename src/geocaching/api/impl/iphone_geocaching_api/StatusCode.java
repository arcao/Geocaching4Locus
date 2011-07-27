package geocaching.api.impl.iphone_geocaching_api;

public enum StatusCode {
	ServiceOffline(-1, "The Web Service is currently offline. Check Status Code for more information."),
	Ok(0, "Method succeeded."),
	Unknown(1, "Unknown or undocumented error occured."),
	UserLoginFailed(2, "Login information is incorrect."),
	UserNotFound(3, "There is no account with that username."),
	UserNotValidated(4, "The account has not been validated."),
	APISessionNotFound(5, "The session was not found in the system."),
	APISessionIsClosed(6, "The session key has expired."),
	APIPoolNotFound(7, "We were unable to locate the pool for this session."),
	APILicenseNotFound(8, "The API license key was not found in the system."),
	APILicenseNotAgreedToTerms(9, "The license key is unavailable until the TOU have been agreed upon."),
	APILicenseNotApproved(10, "This license key has not been approved."),
	UserCommonPoolExceeded(11, "The common request pool has exceeded the maximum number of transactions."),
	APILicenseCommonPoolExceeded(12, "This license's request pool has exceeded the maximum number of transactions."),
	APILicensePerUserPoolExceeded(13, "The license's user request pool has exceeded the maximum number of users."),
	SchemaNotFound(14, "Schema Name was not found. Use GetSchemas method to view available schemas."),
	GCCodeNotFound(15, "The geocache code provided is not in the system."),
	LogIDNotFound(16, "The log identifier is not in the system."),
	TrackableItemIDNotFound(17, "The Trackable item identifier was not found in the system."),
	LogTypeInvalid(18, "The log type indicated does not exist in the system."),
	CacheTypeInvalid(19, "The geocache type provided is not in the system."),
	TrackableItemTypeInvalid(20, "The trackable item type provided is not in the system."),
	PostalCodeNotFound(21, "The postal code provided is not in the system."),
	CoordinateInvalid(22, "The coordinate provided is not valid."),
	RadiusInvalid(24, "The radius provided is not valid."),
	StartPositionTooSmall(25, "The starting position is too small."),
	EndPositionTooLarge(26, "The length of data returned is too large for this operation."),
	DevicePinNoMatch(27, "The phone user's pin (password) does not match"),
	DeviceIdNotfound(28, "The device identifier was not found."),
	GeocacheNotPublished(29, "The geocache hasn't been published yet."),
	SchemaNotSupported(30, "The schema chosen is not available for this operation."),
	GeocacheNotAvailable(31, "The geocache is currently unavailable for viewing."),
	NoResults(32, "No results were found."),
	UserNotAuthorized(33, "This user account is not authorized to use this feature."),
	NoteFieldTooLarge(34, "The text of the note is larger than 4000 characters."),
	TrackingCodeNotValid(35, "The tracking code entered does not match for this trackable item."),
	TrackableNotInContainer(36, "The trackable item is not in the specified container."),
	TrackableNotInInventory(37, "The trackable item is not in the users inventory."),
	TrackableLocked(38, "The trackable item has been locked and cannot be moved."),
	TrackableAlreadyInInventory(39, "The trackable item is already in the user's inventory."),
	DateInvalid(40, "The date entered is not valid."),
	ImageUploadError(41, "There was an error during the image upload process."),
	WptCodeNotValid(42, "The waypoint code provided is not valid."),
	DistanceInvalid(43, "The distance provided is not valid."),
	MaxNotificationsExceeded(44, "You have exceeded the maximum number of notifications for this function.");
	
	private int errorCode;
	private String errorMessage;
	
	StatusCode(int errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public static StatusCode parse(String input) {
		for (StatusCode statusCode : values()) {
			if (statusCode.name().equals(input))
				return statusCode;
		}
		
		return Unknown;
	}
	
	@Override
	public String toString() {	
		return errorMessage;
	}
}
