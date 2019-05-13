package com.arcao.geocaching4locus.data.api.endpoint

import com.arcao.geocaching4locus.data.api.model.*
import com.arcao.geocaching4locus.data.api.model.request.GeocacheExpand
import com.arcao.geocaching4locus.data.api.model.request.GeocacheLogExpand
import com.arcao.geocaching4locus.data.api.model.request.GeocacheSort
import com.arcao.geocaching4locus.data.api.model.request.query.GeocacheQuery
import com.arcao.geocaching4locus.data.api.model.response.TotalCountList
import kotlinx.coroutines.Deferred
import retrofit2.http.*

interface GeocachingApiEndpoint {
    @GET("/v1/geocaches/search")
    fun search(
            @Query("q") q: GeocacheQuery,
            @Query("sort") sort: GeocacheSort? = null,
            @Query("lite") lite: Boolean = true,
            @Query("skip") skip: Int = 0,
            @Query("take") take: Int = 10,
            @Query("fields") fields: String = Geocache.FIELDS_LITE,
            @Query("expand") expand: GeocacheExpand = GeocacheExpand()
    ): Deferred<TotalCountList<Geocache>>

    @GET("/v1/geocaches")
    fun geocaches(
            @Query("referenceCodes") referenceCodes: String,
            @Query("lite") lite: Boolean = true,
            @Query("fields") fields: String = Geocache.FIELDS_LITE,
            @Query("expand") expand: GeocacheExpand = GeocacheExpand()
    ): Deferred<List<Geocache>>


    @GET("/v1/geocache/{referenceCode}")
    fun geocache(
            @Path("referenceCode") referenceCode: String,
            @Query("lite") lite: Boolean = true,
            @Query("fields") fields: String = Geocache.FIELDS_LITE,
            @Query("expand") expand: GeocacheExpand = GeocacheExpand()
    ): Deferred<Geocache>

    @GET("/v1/geocaches/{referenceCode}/images")
    fun geocacheImages(
            @Path("referenceCode") referenceCode: String,
            @Query("skip") skip: Int = 0,
            @Query("take") take: Int = 10,
            @Query("fields") fields: String = Image.FIELDS_ALL
    ): Deferred<TotalCountList<Image>>

    @GET("/v1/geocaches/{referenceCode}/geocacheLogs")
    fun geocacheLogs(
            @Path("referenceCode") referenceCode: String,
            @Query("fields") fields: String = GeocacheLog.FIELDS_ALL,
            @Query("expand") expand: GeocacheLogExpand = GeocacheLogExpand(),
            @Query("skip") skip: Int = 0,
            @Query("take") take: Int = 10
    ): Deferred<TotalCountList<GeocacheLog>>

    fun geocacheTrackables(
            @Path("referenceCode") referenceCode: String,
            @Query("fields") fields: String = Trackable.FIELDS_ALL,
            @Query("skip") skip: Int = 0,
            @Query("take") take: Int = 10
    ): Deferred<TotalCountList<Trackable>>

    @POST("/v1/lists/")
    fun createList(
            @Body list: GeocacheList,
            @Query("fields") fields: String = GeocacheList.FIELDS_ALL
    ): Deferred<GeocacheList>

    @PUT("/v1/lists/{referenceCode}")
    fun updateList(
            @Path("referenceCode") referenceCode: String,
            @Body list: GeocacheList,
            @Query("fields") fields: String = GeocacheList.FIELDS_ALL
    ): Deferred<GeocacheList>

    @DELETE("/v1/lists/{referenceCode}")
    fun deleteList(
            @Path("referenceCode") referenceCode: String
    ): Deferred<Void>

    @GET("/v1/lists/{referenceCode}/geocaches")
    fun listGeocaches(
            @Path("referenceCode") referenceCode : String,
            @Query("fields") fields: String = Geocache.FIELDS_LITE,
            @Query("skip") skip: Int = 0,
            @Query("take") take: Int = 10,
            @Query("lite") lite: Boolean = true,
            @Query("expand") expand: GeocacheExpand = GeocacheExpand()
    ): Deferred<TotalCountList<Geocache>>

    @GET("/v1/users/{referenceCode}")
    fun user(
            @Path("referenceCode") referenceCode: String = "me",
            @Query("fields") fields: String = User.FIELDS_ALL
    ): Deferred<User>

    @GET("/v1/users/{referenceCode}/lists")
    fun userLists(
            @Path("referenceCode") referenceCode: String = "me",
            @Query("types") types: String = "bm",
            @Query("fields") fields: String = GeocacheList.FIELDS_ALL,
            @Query("skip") skip: Int = 0,
            @Query("take") take: Int = 10
    ): Deferred<TotalCountList<GeocacheList>>

    @GET("/v1/friends")
    fun friends(
            @Query("skip") skip: Int = 0,
            @Query("take") take: Int = 10,
            @Query("fields") fields: String = User.FIELDS_ALL
    ): Deferred<TotalCountList<User>>

    @GET("/status/ping")
    fun ping(): Deferred<Void>
}

