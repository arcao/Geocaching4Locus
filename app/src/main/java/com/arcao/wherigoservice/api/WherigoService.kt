package com.arcao.wherigoservice.api

interface WherigoService {
    @Throws(WherigoServiceException::class)
    fun getTime(): Long

    @Throws(WherigoServiceException::class)
    fun getCacheCodeFromGuid(cacheGuid: String): String?
}
