package com.arcao.geocaching4locus.data.api

import com.arcao.geocaching4locus.data.api.endpoint.GeocachingApiEndpoint
import com.arcao.geocaching4locus.data.api.exception.AuthenticationException
import com.arcao.geocaching4locus.data.api.exception.GeocachingApiException
import com.arcao.geocaching4locus.data.api.model.Geocache
import com.arcao.geocaching4locus.data.api.model.GeocacheList
import com.arcao.geocaching4locus.data.api.model.GeocacheLog
import com.arcao.geocaching4locus.data.api.model.Image
import com.arcao.geocaching4locus.data.api.model.Trackable
import com.arcao.geocaching4locus.data.api.model.User
import com.arcao.geocaching4locus.data.api.model.enum.GeocacheListType
import com.arcao.geocaching4locus.data.api.model.request.GeocacheExpand
import com.arcao.geocaching4locus.data.api.model.request.GeocacheLogExpand
import com.arcao.geocaching4locus.data.api.model.request.query.filter.Filter
import com.arcao.geocaching4locus.data.api.model.request.query.queryOf
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class GeocachingApiRepository(private val endpoint: GeocachingApiEndpoint) {

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun search(
        filters: List<Filter>,
        logsCount: Int = 10,
        imageCount: Int = 30,
        trackableCount: Int = 30,
        lite: Boolean = false,
        skip: Int = 0,
        take: Int = 10
    ) = apiCall {
        searchAsync(
            q = queryOf(*filters.toTypedArray()),
            lite = lite,
            expand = GeocacheExpand().all().apply {
                if (!lite) {
                    geocacheLogs = logsCount
                    images = imageCount
                    geocacheLogImages = imageCount
                    trackables = trackableCount
                }
            },
            take = take,
            skip = skip,
            fields = if (lite) Geocache.FIELDS_LITE else Geocache.FIELDS_ALL
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun liveMap(filters: List<Filter>, lite: Boolean = false, skip: Int = 0, take: Int = 10) = apiCall {
        searchAsync(
            q = queryOf(*filters.toTypedArray()),
            lite = lite,
            take = take,
            skip = skip,
            fields = if (lite) Geocache.FIELDS_LITE_LIVEMAP else Geocache.FIELDS_ALL_LIVEMAP
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocache(
        referenceCode: String,
        logsCount: Int = 10,
        imageCount: Int = 30,
        trackableCount: Int = 30,
        lite: Boolean = false
    ) =
        apiCall {
            geocacheAsync(
                referenceCode = referenceCode,
                lite = lite,
                expand = GeocacheExpand().all().apply {
                    if (!lite) {
                        geocacheLogs = logsCount
                        images = imageCount
                        geocacheLogImages = imageCount
                        trackables = trackableCount
                    }
                },
                fields = if (lite) Geocache.FIELDS_LITE else Geocache.FIELDS_ALL
            )
        }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocaches(
        vararg referenceCodes: String,
        logsCount: Int = 10,
        imageCount: Int = 30,
        trackableCount: Int = 30,
        lite: Boolean = false
    ) = apiCall {
        geocachesAsync(
            referenceCodes = referenceCodes.joinToString(","),
            lite = lite,
            expand = GeocacheExpand().all().apply {
                if (!lite) {
                    geocacheLogs = logsCount
                    images = imageCount
                    geocacheLogImages = imageCount
                    trackables = trackableCount
                }
            },
            fields = if (lite) Geocache.FIELDS_LITE else Geocache.FIELDS_ALL
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocacheLogs(referenceCode: String, imageCount: Int = 30, skip: Int = 0, take: Int = 10) = apiCall {
        geocacheLogsAsync(
            referenceCode = referenceCode,
            expand = GeocacheLogExpand().all().apply {
                images = imageCount
            },
            fields = GeocacheLog.FIELDS_MIN,
            skip = skip,
            take = take
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocacheTrackables(referenceCode: String, skip: Int = 0, take: Int = 10) =
        apiCall {
            geocacheTrackablesAsync(
                referenceCode = referenceCode,
                expand = GeocacheLogExpand().all(),
                fields = Trackable.FIELDS_MIN,
                skip = skip,
                take = take
            )
        }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocacheImages(referenceCode: String, skip: Int = 0, take: Int = 10) = apiCall {
        geocacheImagesAsync(
            referenceCode = referenceCode,
            fields = Image.FIELDS_MIN,
            skip = skip,
            take = take
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun user(referenceCode: String = "me") = apiCall {
        userAsync(
            referenceCode = referenceCode
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun userLimits(referenceCode: String = "me") = apiCall {
        userAsync(
            referenceCode = referenceCode,
            fields = User.FIELDS_LIMITS
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun createList(list: GeocacheList) = apiCall {
        createListAsync(
            list = list
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun updateList(referenceCode: String? = null, list: GeocacheList) = apiCall {
        updateListAsync(
            referenceCode = referenceCode ?: list.referenceCode,
            list = list
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun deleteList(referenceCode: String) = apiCall {
        deleteListAsync(
            referenceCode = referenceCode
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun userLists(
        referenceCode: String = "me",
        types: Set<GeocacheListType> = setOf(GeocacheListType.BOOKMARK),
        skip: Int = 0,
        take: Int = 10
    ) = apiCall {
        userListsAsync(
            referenceCode = referenceCode,
            types = types.joinToString(","),
            skip = skip,
            take = take
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun listGeocaches(
        referenceCode: String,
        skip: Int = 0,
        take: Int = 10,
        logsCount: Int = 10,
        lite: Boolean = false
    ) = apiCall {
        listGeocachesAsync(
            referenceCode = referenceCode,
            fields = if (lite) Geocache.FIELDS_LITE else Geocache.FIELDS_ALL,
            skip = skip,
            take = take,
            lite = lite,
            expand = GeocacheExpand().all().apply { geocacheLogs = logsCount }
        )
    }

    private suspend inline fun <T : Any> apiCall(crossinline body: GeocachingApiEndpoint.() -> Deferred<T>): T =
        withContext(Dispatchers.IO) {
            endpoint.body().await()
        }
}