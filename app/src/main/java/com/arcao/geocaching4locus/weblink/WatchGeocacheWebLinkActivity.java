package com.arcao.geocaching4locus.weblink;

import android.net.Uri;
import android.text.TextUtils;

import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.BuildConfig;

import java.util.Locale;

import locus.api.objects.extra.Waypoint;

public class WatchGeocacheWebLinkActivity extends AbstractWebLinkActivity {
    private static final String URL_FORMAT = "https://www.geocaching.com/my/watchlist.aspx?w=%d";
    private static final String URL_FORMAT_STAGING = "https://staging.geocaching.com/my/watchlist.aspx?w=%d";

    @Override
    protected Uri getWebLink(Waypoint waypoint) {
        if (waypoint == null || waypoint.gcData == null || TextUtils.isEmpty(waypoint.gcData.getCacheID()))
            return null;

        long cacheId = GeocachingUtils.cacheCodeToCacheId(waypoint.gcData.getCacheID());

        if (BuildConfig.GEOCACHING_API_STAGING) {
            return Uri.parse(String.format(Locale.ROOT, URL_FORMAT_STAGING, cacheId));
        } else {
            return Uri.parse(String.format(Locale.ROOT, URL_FORMAT, cacheId));
        }
    }
}
