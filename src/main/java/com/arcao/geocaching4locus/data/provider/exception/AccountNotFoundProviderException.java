package com.arcao.geocaching4locus.data.provider.exception;

public class AccountNotFoundProviderException extends ProviderException {
	private static final long serialVersionUID = -1337993742267453164L;

	public AccountNotFoundProviderException(String message) {
		super(message, ProviderExceptionCategory.CREDENTIAL);
	}
}
