package com.arcao.wherigoservice.api;

import java.io.IOException;

public class WherigoServiceException extends IOException {
	private static final long serialVersionUID = -1298236380965518822L;

	public static final int ERROR_OK = 0;
	public static final int ERROR_INVALID_CREDITIALS = 1;
	public static final int ERROR_INVALID_SESSION = 2;
	public static final int ERROR_CARTRIDGE_NOT_FOUND = 10;
	public static final int ERROR_CACHE_NOT_FOUND = 11;
	public static final int ERROR_API_ERROR = 500;
	public static final int ERROR_CONNECTION_ERROR = 501;

	private final int code;

	public WherigoServiceException(int code, String message) {
		this(code, message, null);
	}

	public WherigoServiceException(int code, String message, Throwable t) {
		super(message);
		initCause(t);
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String toString() {
		return super.toString() + " (" + code + ")";
	}
}
