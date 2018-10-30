package com.arcao.geocaching4locus.weblink;

import android.net.Uri;
import android.text.TextUtils;

import com.arcao.geocaching.api.util.GeocachingUtils;
import com.arcao.geocaching4locus.BuildConfig;

import java.util.Locale;

import locus.api.objects.extra.Point;

public class WatchGeocacheWebLinkActivity extends AbstractWebLinkActivity {
    private static final String URL_FORMAT = "https://www.geocaching.com/my/watchlist.aspx?w=%d";
    private static final String URL_FORMAT_STAGING = "https://staging.geocaching.com/my/watchlist.aspx?w=%d";

    @Override
    protected Uri getWebLink(Point point) {
        if (point == null || point.gcData == null || TextUtils.isEmpty(point.gcData.getCacheID()))
            return null;

        long cacheId = GeocachingUtils.cacheCodeToCacheId(point.gcData.getCacheID());

        if (BuildConfig.GEOCACHING_API_STAGING) {
            return Uri.parse(String.format(Locale.ROOT, URL_FORMAT_STAGING, cacheId));
        } else {
            return Uri.parse(String.format(Locale.ROOT, URL_FORMAT, cacheId));
        }
    }
}
