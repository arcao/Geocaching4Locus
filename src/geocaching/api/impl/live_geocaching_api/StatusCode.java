package geocaching.api.impl.live_geocaching_api;

public enum StatusCode {
	AccountNotFound(14),
  ApplicationNotApproved(7),
  ApplicationTokenNotValid(6),
  CacheNotAvailable(8),
  CacheNoteDeleteFailed(27),
  CacheNoteUpdateFailed(26),
  CannotLogToLockedCache(11),
  Fail(1),
  GeocacheCodeIsNotValid(12),
  GeocacheNotFound(13),
  ImageUploadFailed(25),
  InvalidDateTime(15),
  InvalidNote(16),
  LogLoadFailed(23),
  LogNotOwned(24),
  LogTypeNotProvided(29),
  MethodNotSupplied(90),
  NoClampingFilterProvided(89),
  NoImageData(21),
  NotAuthorized(2),
  NotImplemented(9),
  NoteTooLong(17),
  OK(0),
  PhotoCaptionTooLong(19),
  PhotoDescriptionTooLong(20),
  ProfileLoadFailed(22),
  SessionExpired(18),
  SouvenirNoPublicGuidFail(34),
  TrackableAlreadyHeld(41),
  TrackableAlreadyMarkedAsMissing(42),
  TrackableIsArchived(39),
  TrackableLogFailed(28),
  TrackableLogTypeInvalid(35),
  TrackableMustBeHeldByUser(40),
  TrackableMustBeInCache(44),
  TrackableRequiredCacheCode(45),
  TrackableUnableToBeMarkedMissing(43),
  TrackingCodeInvalid(38),
  TrackingCodeRequired(37),
  TravelBugCodeInvalid(36),
  UserAccountProblem(3),
  UserDidNotAuthorize(4),
  UserTokenNotValid(5),
  ValidateNotFoundByUserBasicMembershipFail(32),
  ValidateNotFoundByUserNoBasicMembershipFail(31),
  ValidateNotFoundByUserPremiumMembershipFail(33),
  ValidateSearchDataFailed(30),
  WptLogTypeIdInvalid(10);
	
	private int code;
	
	private StatusCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static StatusCode parseStatusCode(int code) {
		for (StatusCode statusCode : values()) {
			if (statusCode.getCode() == code)
				return statusCode;
		}
		
		return Fail;
	}
}
