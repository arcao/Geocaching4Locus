package com.arcao.geocaching4locus.data.api

import com.arcao.geocaching4locus.data.api.endpoint.GeocachingApiEndpoint
import com.arcao.geocaching4locus.data.api.exception.AuthenticationException
import com.arcao.geocaching4locus.data.api.exception.GeocachingApiException
import com.arcao.geocaching4locus.data.api.model.Geocache
import com.arcao.geocaching4locus.data.api.model.GeocacheList
import com.arcao.geocaching4locus.data.api.model.enum.GeocacheListType
import com.arcao.geocaching4locus.data.api.model.request.GeocacheExpand
import com.arcao.geocaching4locus.data.api.model.request.query.filter.Filter
import com.arcao.geocaching4locus.data.api.model.request.query.queryOf
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class GeocachingApiRepository(private val endpoint: GeocachingApiEndpoint) {

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun search(filters: Set<Filter>, logsCount: Int = 10, lite: Boolean = false, skip: Int = 0, take: Int = 10) = apiCall {
        search(
                q = queryOf(*filters.toTypedArray()),
                lite = lite,
                expand = GeocacheExpand().all().apply { geocacheLogs = logsCount },
                take = take,
                skip = skip,
                fields = if (lite) Geocache.FIELDS_LITE else Geocache.FIELDS_ALL
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun liveMap(filters: Set<Filter>, lite: Boolean = false, skip: Int = 0, take: Int = 10) = apiCall {
        search(
                q = queryOf(*filters.toTypedArray()),
                lite = lite,
                take = take,
                skip = skip,
                fields = if (lite) Geocache.FIELDS_LITE_LIVEMAP else Geocache.FIELDS_ALL_LIVEMAP
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocache(referenceCode: String, logsCount: Int = 10, lite: Boolean = false) = apiCall {
        geocache(
                referenceCode = referenceCode,
                lite = lite,
                expand = GeocacheExpand().all().apply { geocacheLogs = logsCount },
                fields = if (lite) Geocache.FIELDS_LITE else Geocache.FIELDS_ALL
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocaches(vararg referenceCodes: String, logsCount: Int = 10, lite: Boolean = false) = apiCall {
        geocaches(
                referenceCodes = referenceCodes.joinToString(","),
                lite = lite,
                expand = GeocacheExpand().all().apply { geocacheLogs = logsCount },
                fields = if (lite) Geocache.FIELDS_LITE else Geocache.FIELDS_ALL
        )
    }


    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocacheLogs(referenceCode: String, skip: Int = 0, take: Int = 10) = apiCall {
        geocacheLogs(
                referenceCode = referenceCode,
                skip = skip,
                take = take
        )
    }


    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocacheTrackables(referenceCode: String, skip: Int = 0, take: Int = 10) = apiCall {
        geocacheTrackables(
                referenceCode = referenceCode,
                skip = skip,
                take = take
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun geocacheImages(referenceCode: String, skip: Int = 0, take: Int = 10) = apiCall {
        geocacheImages(
                referenceCode = referenceCode,
                skip = skip,
                take = take
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun user(referenceCode: String = "me") = apiCall {
        user(
                referenceCode = referenceCode
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun createList(list: GeocacheList) = apiCall {
        createList(
                list = list
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun updateList(referenceCode: String? = null, list: GeocacheList) = apiCall {
        updateList(
                referenceCode = referenceCode ?: list.referenceCode,
                list = list
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun deleteList(referenceCode: String) = apiCall {
        deleteList(
                referenceCode = referenceCode
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun userLists(referenceCode: String = "me", types: Set<GeocacheListType> = setOf(GeocacheListType.BOOKMARK), skip: Int = 0, take: Int = 10) = apiCall {
        userLists(
                referenceCode = referenceCode,
                types = types.joinToString(","),
                skip = skip,
                take = take
        )
    }

    @Throws(GeocachingApiException::class, AuthenticationException::class, IOException::class)
    suspend fun listGeocaches(referenceCode: String, skip: Int = 0, take: Int = 10, logsCount: Int = 10, lite: Boolean = false) = apiCall {
        listGeocaches(
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