package com.arcao.geocaching4locus.base.util

import kotlin.math.max
import kotlin.math.min

object DownloadingUtil {
    /* Adaptive downloading configuration */
    const val MIN_REQUEST_SIZE = 10
    const val REQUEST_SIZE_INCREMENT = 20
    const val REQUEST_SIZE_DECREMENT = REQUEST_SIZE_INCREMENT
    const val MIN_REQUEST_TIME_MS =
        3500 // more than time required for 30 calls per minute
    const val MAX_REQUEST_TIME_MS = 10000

    fun computeRequestSize(
        currentRequestSize: Int,
        maxRequestSize: Int,
        startTimeMillis: Long
    ): Int {
        var requestSize = currentRequestSize
        val requestDuration = System.currentTimeMillis() - startTimeMillis

        // keep the request time between MIN_REQUEST_TIME_MS and MAX_REQUEST_TIME_MS
        if (requestDuration < MIN_REQUEST_TIME_MS)
            requestSize += REQUEST_SIZE_INCREMENT

        if (requestDuration > MAX_REQUEST_TIME_MS)
            requestSize -= REQUEST_SIZE_DECREMENT

        // keep the value in a range
        requestSize = max(requestSize, MIN_REQUEST_SIZE)
        requestSize = min(requestSize, maxRequestSize)

        return requestSize
    }
}
