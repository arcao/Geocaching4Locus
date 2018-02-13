package com.arcao.geocaching4locus.base.util;

import com.arcao.geocaching4locus.base.constants.AppConstants;

/**
 * Created by Arcao on 12.02.2018.
 */
public final class DownloadingUtil {
    private DownloadingUtil() {
    }

    public static int computeItemsPerRequest(int currentItemsPerRequest, long startTimeMillis) {
        int itemsPerRequest = currentItemsPerRequest;
        long requestDuration = System.currentTimeMillis() - startTimeMillis;

        // keep the request time between ADAPTIVE_DOWNLOADING_MIN_TIME_MS and ADAPTIVE_DOWNLOADING_MAX_TIME_MS
        if (requestDuration < AppConstants.ADAPTIVE_DOWNLOADING_MIN_TIME_MS)
            itemsPerRequest += AppConstants.ADAPTIVE_DOWNLOADING_STEP;

        if (requestDuration > AppConstants.ADAPTIVE_DOWNLOADING_MAX_TIME_MS)
            itemsPerRequest -= AppConstants.ADAPTIVE_DOWNLOADING_STEP;

        // keep the value in a range
        itemsPerRequest = Math.max(itemsPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MIN_ITEMS);
        itemsPerRequest = Math.min(itemsPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MAX_ITEMS);

        return itemsPerRequest;
    }
}
