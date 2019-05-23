package com.arcao.geocaching4locus.data.api.endpoint

import com.arcao.geocaching4locus.data.api.model.Geocache
import com.arcao.geocaching4locus.data.api.model.GeocacheList
import com.arcao.geocaching4locus.data.api.model.GeocacheLog
import com.arcao.geocaching4locus.data.api.model.Image
import com.arcao.geocaching4locus.data.api.model.Trackable
import com.arcao.geocaching4locus.data.api.model.User
import com.arcao.geocaching4locus.data.api.model.request.GeocacheExpand
import com.arcao.geocaching4locus.data.api.model.request.GeocacheLogExpand
import com.arcao.geocaching4locus.data.api.model.request.GeocacheSort
import com.arcao.geocaching4locus.data.api.model.request.query.GeocacheQuery
import com.arcao.geocaching4locus.data.api.model.response.PagedList
import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GeocachingApiEndpoint {
    @GET("/v1/geocaches/search")
    fun searchAsync(
        @Query("q") q: GeocacheQuery,
        @Query("sort") sort: GeocacheSort? = null,
        @Query("lite") lite: Boolean = true,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 10,
        @Query("fields") fields: String = Geocache.FIELDS_LITE,
        @Query("expand") expand: GeocacheExpand = GeocacheExpand()
    ): Deferred<PagedList<Geocache>>

    @GET("/v1/geocaches")
    fun geocachesAsync(
        @Query("referenceCodes") referenceCodes: String,
        @Query("lite") lite: Boolean = true,
        @Query("fields") fields: String = Geocache.FIELDS_LITE,
        @Query("expand") expand: GeocacheExpand = GeocacheExpand()
    ): Deferred<List<Geocache>>

    @GET("/v1/geocaches/{referenceCode}")
    fun geocacheAsync(
        @Path("referenceCode") referenceCode: String,
        @Query("lite") lite: Boolean = true,
        @Query("fields") fields: String = Geocache.FIELDS_LITE,
        @Query("expand") expand: GeocacheExpand = GeocacheExpand()
    ): Deferred<Geocache>

    @GET("/v1/geocaches/{referenceCode}/images")
    fun geocacheImagesAsync(
        @Path("referenceCode") referenceCode: String,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 10,
        @Query("fields") fields: String = Image.FIELDS_ALL
    ): Deferred<PagedList<Image>>

    @GET("/v1/geocaches/{referenceCode}/geocacheLogs")
    fun geocacheLogsAsync(
        @Path("referenceCode") referenceCode: String,
        @Query("fields") fields: String = GeocacheLog.FIELDS_ALL,
        @Query("expand") expand: GeocacheLogExpand = GeocacheLogExpand(),
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 10
    ): Deferred<PagedList<GeocacheLog>>

    @GET("/v1/trackables/{referenceCode}")
    fun geocacheTrackablesAsync(
        @Path("referenceCode") referenceCode: String,
        @Query("fields") fields: String = Trackable.FIELDS_ALL,
        @Query("expand") expand: GeocacheLogExpand = GeocacheLogExpand(),
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 10
    ): Deferred<PagedList<Trackable>>

    @POST("/v1/lists/")
    fun createListAsync(
        @Body list: GeocacheList,
        @Query("fields") fields: String = GeocacheList.FIELDS_ALL
    ): Deferred<GeocacheList>

    @PUT("/v1/lists/{referenceCode}")
    fun updateListAsync(
        @Path("referenceCode") referenceCode: String,
        @Body list: GeocacheList,
        @Query("fields") fields: String = GeocacheList.FIELDS_ALL
    ): Deferred<GeocacheList>

    @DELETE("/v1/lists/{referenceCode}")
    fun deleteListAsync(
        @Path("referenceCode") referenceCode: String
    ): Deferred<Void>

    @GET("/v1/lists/{referenceCode}/geocaches")
    fun listGeocachesAsync(
        @Path("referenceCode") referenceCode: String,
        @Query("fields") fields: String = Geocache.FIELDS_LITE,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 10,
        @Query("lite") lite: Boolean = true,
        @Query("expand") expand: GeocacheExpand = GeocacheExpand()
    ): Deferred<PagedList<Geocache>>

    @GET("/v1/users/{referenceCode}")
    fun userAsync(
        @Path("referenceCode") referenceCode: String = "me",
        @Query("fields") fields: String = User.FIELDS_ALL
    ): Deferred<User>

    @GET("/v1/users/{referenceCode}/lists")
    fun userListsAsync(
        @Path("referenceCode") referenceCode: String = "me",
        @Query("types") types: String = "bm",
        @Query("fields") fields: String = GeocacheList.FIELDS_ALL,
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 10
    ): Deferred<PagedList<GeocacheList>>

    @GET("/v1/friends")
    fun friendsAsync(
        @Query("skip") skip: Int = 0,
        @Query("take") take: Int = 10,
        @Query("fields") fields: String = User.FIELDS_ALL
    ): Deferred<PagedList<User>>

    @GET("/status/ping")
    fun pingAsync(): Deferred<Void>
}

