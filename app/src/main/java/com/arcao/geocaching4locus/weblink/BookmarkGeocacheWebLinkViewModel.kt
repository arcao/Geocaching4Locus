package com.arcao.geocaching4locus.weblink

import android.net.Uri
import com.arcao.geocaching.api.data.type.GeocacheType
import com.arcao.geocaching4locus.BuildConfig
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import locus.api.objects.extra.Point
import locus.api.objects.geocaching.GeocachingData
import java.util.*
import java.util.regex.Pattern

class BookmarkGeocacheWebLinkViewModel(
    accountManager: AccountManager,
    getPointFromGeocacheCodeUseCase: GetPointFromGeocacheCodeUseCase,
    exceptionHandler: ExceptionHandler,
    dispatcherProvider: CoroutinesDispatcherProvider
) : WebLinkViewModel(accountManager, getPointFromGeocacheCodeUseCase, exceptionHandler, dispatcherProvider) {

    override val isPremiumMemberRequired: Boolean
        get() = true

    override fun isRefreshRequired(point: Point): Boolean {
        return point.gcData != null &&
                !point.gcData.cacheUrl.isNullOrEmpty() &&
                getGuid(point.gcData.cacheUrl) == null
    }

    override fun getWebLink(point: Point): Uri {
        val guid = getGuid(point.gcData.cacheUrl)
        val cacheType = getCacheType(point.gcData.type)

        return if (BuildConfig.GEOCACHING_API_STAGING) {
            Uri.parse(String.format(Locale.ROOT, URL_FORMAT_STAGING, guid, cacheType))
        } else {
            Uri.parse(String.format(Locale.ROOT, URL_FORMAT, guid, cacheType))
        }
    }

    private fun getGuid(cacheUrl: String): String? {
        val matcher = GUID_URL_PATTERN.matcher(cacheUrl)

        return if (matcher.find() && matcher.groupCount() >= 1) {
            matcher.group(1)
        } else null
    }

    private fun getCacheType(cacheType: Int): Int {
        when (cacheType) {
            GeocachingData.CACHE_TYPE_CACHE_IN_TRASH_OUT -> return GeocacheType.CacheInTrashOutEvent.id
            GeocachingData.CACHE_TYPE_EARTH -> return GeocacheType.Earth.id
            GeocachingData.CACHE_TYPE_EVENT -> return GeocacheType.Event.id
            GeocachingData.CACHE_TYPE_GPS_ADVENTURE -> return GeocacheType.GpsAdventuresExhibit.id
            GeocachingData.CACHE_TYPE_GROUNDSPEAK -> return GeocacheType.GroudspeakHQ.id
            GeocachingData.CACHE_TYPE_LF_CELEBRATION -> return GeocacheType.GroudspeakLostAndFoundCelebration.id
            GeocachingData.CACHE_TYPE_LETTERBOX -> return GeocacheType.LetterboxHybrid.id
            GeocachingData.CACHE_TYPE_LOCATIONLESS -> return GeocacheType.Locationless.id
            GeocachingData.CACHE_TYPE_LF_EVENT -> return GeocacheType.LostAndFoundEvent.id
            GeocachingData.CACHE_TYPE_MEGA_EVENT -> return GeocacheType.MegaEvent.id
            GeocachingData.CACHE_TYPE_MULTI -> return GeocacheType.Multi.id
            GeocachingData.CACHE_TYPE_PROJECT_APE -> return GeocacheType.ProjectApe.id
            GeocachingData.CACHE_TYPE_TRADITIONAL -> return GeocacheType.Traditional.id
            GeocachingData.CACHE_TYPE_MYSTERY -> return GeocacheType.Mystery.id
            GeocachingData.CACHE_TYPE_VIRTUAL -> return GeocacheType.Virtual.id
            GeocachingData.CACHE_TYPE_WEBCAM -> return GeocacheType.Webcam.id
            GeocachingData.CACHE_TYPE_WHERIGO -> return GeocacheType.Wherigo.id
            GeocachingData.CACHE_TYPE_GIGA_EVENT -> return GeocacheType.GigaEvent.id
            else -> return GeocacheType.Mystery.id
        }
    }

    companion object {
        private const val URL_FORMAT = "https://www.geocaching.com/bookmarks/mark.aspx?guid=%s&WptTypeID=%d"
        private const val URL_FORMAT_STAGING = "https://staging.geocaching.com/bookmarks/mark.aspx?guid=%s&WptTypeID=%d"
        private val GUID_URL_PATTERN = Pattern.compile("guid=([a-f0-9-]+)", Pattern.CASE_INSENSITIVE)
    }
}
