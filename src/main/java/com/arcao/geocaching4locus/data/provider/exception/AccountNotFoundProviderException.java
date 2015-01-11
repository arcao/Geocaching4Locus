package com.arcao.geocaching4locus.data.provider.exception;

public class AccountNotFoundProviderException extends ProviderException {
	public AccountNotFoundProviderException(String message) {
		super(message, ProviderExceptionCategory.CREDENTIAL);
	}
}
