package com.arcao.geocaching4locus.weblink;

import android.net.Uri;
import android.text.TextUtils;

import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching4locus.BuildConfig;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import locus.api.objects.extra.Point;

import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_CACHE_IN_TRASH_OUT;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_EARTH;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_EVENT;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_GIGA_EVENT;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_GPS_ADVENTURE;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_GROUNDSPEAK;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_LETTERBOX;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_LF_CELEBRATION;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_LF_EVENT;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_LOCATIONLESS;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_MEGA_EVENT;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_MULTI;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_MYSTERY;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_PROJECT_APE;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_TRADITIONAL;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_VIRTUAL;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_WEBCAM;
import static locus.api.objects.geocaching.GeocachingData.CACHE_TYPE_WHERIGO;

public class BookmarkGeocacheWebLinkActivity extends AbstractWebLinkActivity {
    private static final String URL_FORMAT = "https://www.geocaching.com/bookmarks/mark.aspx?guid=%s&WptTypeID=%d";
    private static final String URL_FORMAT_STAGING = "https://staging.geocaching.com/bookmarks/mark.aspx?guid=%s&WptTypeID=%d";
    private static final Pattern GUID_URL_PATTERN = Pattern.compile("guid=([a-f0-9-]+)", Pattern.CASE_INSENSITIVE);

    @Override
    protected boolean isRefreshRequired(Point point) {
        return !(point == null || point.gcData == null || TextUtils.isEmpty(point.gcData.getCacheUrl())) && getGuid(point.gcData.getCacheUrl()) == null;
    }

    @Override
    protected boolean isPremiumMemberRequired() {
        return true;
    }

    @Override
    protected Uri getWebLink(Point point) {
        if (point == null || point.gcData == null || TextUtils.isEmpty(point.gcData.getCacheUrl()))
            return null;

        String guid = getGuid(point.gcData.getCacheUrl());
        int cacheType = getCacheType(point.gcData.getType());

        if (BuildConfig.GEOCACHING_API_STAGING) {
            return Uri.parse(String.format(Locale.ROOT, URL_FORMAT_STAGING, guid, cacheType));
        } else {
            return Uri.parse(String.format(Locale.ROOT, URL_FORMAT, guid, cacheType));
        }
    }

    private String getGuid(String cacheUrl) {
        Matcher matcher = GUID_URL_PATTERN.matcher(cacheUrl);

        if (matcher.find() && matcher.groupCount() >= 1) {
            return matcher.group(1);
        }

        return null;
    }

    private int getCacheType(int cacheType) {
        switch (cacheType) {
            case CACHE_TYPE_CACHE_IN_TRASH_OUT:
                return GeocacheType.CacheInTrashOutEvent.id;
            case CACHE_TYPE_EARTH:
                return GeocacheType.Earth.id;
            case CACHE_TYPE_EVENT:
                return GeocacheType.Event.id;
            case CACHE_TYPE_GPS_ADVENTURE:
                return GeocacheType.GpsAdventuresExhibit.id;
            case CACHE_TYPE_GROUNDSPEAK:
                return GeocacheType.GroudspeakHQ.id;
            case CACHE_TYPE_LF_CELEBRATION:
                return GeocacheType.GroudspeakLostAndFoundCelebration.id;
            case CACHE_TYPE_LETTERBOX:
                return GeocacheType.LetterboxHybrid.id;
            case CACHE_TYPE_LOCATIONLESS:
                return GeocacheType.Locationless.id;
            case CACHE_TYPE_LF_EVENT:
                return GeocacheType.LostAndFoundEvent.id;
            case CACHE_TYPE_MEGA_EVENT:
                return GeocacheType.MegaEvent.id;
            case CACHE_TYPE_MULTI:
                return GeocacheType.Multi.id;
            case CACHE_TYPE_PROJECT_APE:
                return GeocacheType.ProjectApe.id;
            case CACHE_TYPE_TRADITIONAL:
                return GeocacheType.Traditional.id;
            case CACHE_TYPE_MYSTERY:
                return GeocacheType.Mystery.id;
            case CACHE_TYPE_VIRTUAL:
                return GeocacheType.Virtual.id;
            case CACHE_TYPE_WEBCAM:
                return GeocacheType.Webcam.id;
            case CACHE_TYPE_WHERIGO:
                return GeocacheType.Wherigo.id;
            case CACHE_TYPE_GIGA_EVENT:
                return GeocacheType.GigaEvent.id;
            default:
                return GeocacheType.Mystery.id;
        }
    }
}
