package geocaching.api.exception;

public class SessionInvalidException extends Exception {
	private static final long serialVersionUID = 979749036657920639L;

	public SessionInvalidException() {
		super();
	}

	public SessionInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

	public SessionInvalidException(String message) {
		super(message);
	}

	public SessionInvalidException(Throwable cause) {
		super(cause);
	}
	

}
