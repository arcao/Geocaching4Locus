package com.arcao.geocaching4locus.error.exception;

public class CacheNotFoundException extends Exception {
	private static final long serialVersionUID = 1435947072951481547L;

	private final String[] cacheCodes;

	public CacheNotFoundException(String... cacheCode) {
		this.cacheCodes = cacheCode;
	}

	public String[] getCacheCodes() {
		return cacheCodes;
	}
}