package com.arcao.geocaching4locus.base.constants;

import android.net.Uri;
import android.os.Build;
import android.util.Base64;

import locus.api.android.utils.LocusUtils;

public interface AppConstants {
    String OAUTH_CALLBACK_URL = "https://geocaching4locus.eu/oauth";

    Uri MANUAL_URI = Uri.parse("https://geocaching4locus.eu/manual/");
    Uri WEBSITE_URI = Uri.parse("https://geocaching4locus.eu/");
    Uri FACEBOOK_URI = Uri.parse("https://www.facebook.com/Geocaching4Locus");
    Uri GPLUS_URI = Uri.parse("https://plus.google.com/+Geocaching4locusEu");
    Uri GEOCACHING_LIVE_URI = Uri.parse("http://www.geocaching.com/live");

    // Saved in Base64 because Google Play doesn't allow donation via Paypal.
    // This will prevent it to autodetect by robot.
    // params: %s = currency code (ISO-4217)
    String DONATE_PAYPAL_URI = new String(Base64.decode(
            "aHR0cHM6Ly93d3cucGF5cGFsLmNvbS9jZ2ktYmluL3dlYnNjcj9jbWQ9X2RvbmF0aW9ucyZidXNpbmVzcz1hcmNhbyUlNDBhcmNhbyUlMmVjb20mbGM9Q1omaXRlbV9uYW1lPUdlb2NhY2hpbmc0TG9jdXMmaXRlbV9udW1iZXI9ZzRsJmN1cnJlbmN5X2NvZGU9JXMmYm49UFAlJTJkRG9uYXRpb25zQkYlJTNhYnRuX2RvbmF0ZUNDX0xHJSUyZWdpZiUlM2FOb25Ib3N0ZWQ=",
            Base64.DEFAULT));

    String LOCUS_MIN_VERSION = "3.8.0";
    LocusUtils.VersionCode LOCUS_MIN_VERSION_CODE = LocusUtils.VersionCode.UPDATE_09;

    /* Adaptive downloading configuration */
    int ADAPTIVE_DOWNLOADING_MIN_CACHES = 5;
    int ADAPTIVE_DOWNLOADING_MAX_CACHES = 50;
    int ADAPTIVE_DOWNLOADING_STEP = 5;
    int ADAPTIVE_DOWNLOADING_MIN_TIME_MS = 3500;
    int ADAPTIVE_DOWNLOADING_MAX_TIME_MS = 10000;
    int CACHES_PER_REQUEST = ADAPTIVE_DOWNLOADING_MIN_CACHES;

    int SECONDS_PER_MINUTE = 60;

    /* Search nearest cache count configuration */
    int DOWNLOADING_COUNT_OF_CACHES_DEFAULT = 20;
    int DOWNLOADING_COUNT_OF_CACHES_MAX = 500;
    int DOWNLOADING_COUNT_OF_CACHES_MAX_LOW_MEMORY = 200;
    int DOWNLOADING_COUNT_OF_CACHES_STEP_DEFAULT = 10;

    long LOW_MEMORY_THRESHOLD = 16777216;

    String UPDATE_WITH_LOGS_COMPONENT = "com.arcao.geocaching4locus.UpdateWithLogsActivity";
    int LOGS_PER_REQUEST = 30;
    int LOGS_TO_UPDATE_MAX = 100;

    float MILES_PER_KILOMETER = 1.609344F;
    float DISTANCE_KM_DEFAULT = 50;
    float DISTANCE_MILES_DEFAULT = 50 / MILES_PER_KILOMETER;

    // restrictions in Geocaching Live Service
    float DISTANCE_KM_MIN = 0.1F;
    float DISTANCE_KM_MAX = 50;

    String PREMIUM_CHARACTER = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) ? new String(Character.toChars(0x1F451)) : "(PM)";

    int NOTIFICATION_ID_LIVEMAP = 1;

    int LIVEMAP_REQUESTS = 5;
    int LIVEMAP_CACHES_PER_REQUEST = 50;
    int LIVEMAP_CACHES_COUNT = LIVEMAP_REQUESTS * LIVEMAP_CACHES_PER_REQUEST;
    int LIVEMAP_DISTANCE = 60000;
    String LIVEMAP_PACK_WAYPOINT_PREFIX = "LiveMap|";

}
