package com.arcao.geocaching4locus.error.exception

class LocusMapRuntimeException(cause: Throwable) : RuntimeException(cause) {
    companion object {
        private const val serialVersionUID = -4019163571054565979L
    }
}
