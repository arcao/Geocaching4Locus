package com.arcao.geocaching4locus.error.exception

class CacheNotFoundException(vararg cacheCode: String) : Exception() {
    val cacheCodes: Array<out String> = cacheCode

    companion object {
        private const val serialVersionUID = 1435947072951481547L
    }
}