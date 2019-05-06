package com.arcao.geocaching4locus.base.util

import com.arcao.geocaching4locus.base.constants.AppConstants
import kotlin.math.max
import kotlin.math.min

object DownloadingUtil {
    fun computeItemsPerRequest(currentItemsPerRequest: Int, startTimeMillis: Long): Int {
        var itemsPerRequest = currentItemsPerRequest
        val requestDuration = System.currentTimeMillis() - startTimeMillis

        // keep the request time between ADAPTIVE_DOWNLOADING_MIN_TIME_MS and ADAPTIVE_DOWNLOADING_MAX_TIME_MS
        if (requestDuration < AppConstants.ADAPTIVE_DOWNLOADING_MIN_TIME_MS)
            itemsPerRequest += AppConstants.ADAPTIVE_DOWNLOADING_STEP

        if (requestDuration > AppConstants.ADAPTIVE_DOWNLOADING_MAX_TIME_MS)
            itemsPerRequest -= AppConstants.ADAPTIVE_DOWNLOADING_STEP

        // keep the value in a range
        itemsPerRequest = max(itemsPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MIN_ITEMS)
        itemsPerRequest = min(itemsPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MAX_ITEMS)

        return itemsPerRequest
    }
}
