package com.arcao.geocaching4locus.data.provider.exception;

public class ProviderException extends Exception {
	private final ProviderExceptionCategory exceptionCategory;
	private String userMessage;

	public ProviderException(String message, ProviderExceptionCategory exceptionCategory, Throwable cause) {
		super(message, cause);
		this.exceptionCategory = exceptionCategory;
	}

	public ProviderException(String message, ProviderExceptionCategory exceptionCategory) {
		this(message, exceptionCategory, null);
	}

	public ProviderExceptionCategory getExceptionCategory() {
		return exceptionCategory;
	}

	public ProviderException setUserMessage(String userMessage) {
		this.userMessage = userMessage;
		return this;
	}

	public String getUserMessage() {
		return userMessage;
	}
}
