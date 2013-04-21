package com.arcao.wherigoservice.api;

public interface WherigoService {
	String getCacheCodeFromGuid(String cacheGuid) throws WherigoServiceException;
}
