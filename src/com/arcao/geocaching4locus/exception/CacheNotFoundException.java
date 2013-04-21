package com.arcao.geocaching4locus.exception;

public class CacheNotFoundException extends Exception {
	private static final long serialVersionUID = 1435947072951481547L;

	protected String cacheCode;

	public CacheNotFoundException(String cacheCode) {
		this.cacheCode = cacheCode;
	}

	public String getCacheCode() {
		return cacheCode;
	}
}