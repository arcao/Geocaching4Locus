package com.arcao.geocaching4locus.weblink.usecase

import android.content.Context
import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching4locus.authentication.task.GeocachingApiLoginTask
import com.arcao.geocaching4locus.error.exception.CacheNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import locus.api.mapper.DataMapper
import locus.api.objects.extra.Point

class GetPointFromGeocacheCodeUseCase(
        private val context: Context,
        private val geocachingApi: GeocachingApi,
        private val mapper : DataMapper
) {
    suspend operator fun invoke(geocacheCode : String, liteData : Boolean = true) : Point {
        return withContext(Dispatchers.IO) {
            GeocachingApiLoginTask.create(context, geocachingApi).perform();

            val resultQuality = if (liteData)
                GeocachingApi.ResultQuality.LITE
            else
                GeocachingApi.ResultQuality.FULL

            val geocache = geocachingApi.getGeocache(resultQuality, geocacheCode, 0, 0)
                    ?: throw CacheNotFoundException(geocacheCode);

            mapper.createLocusPoint(geocache)
        }
    }
}

