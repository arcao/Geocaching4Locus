package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkEntity
import kotlinx.coroutines.withContext

class GetBookmarkUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLoginUseCase: GeocachingApiLoginUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(guid : String): List<BookmarkEntity> = withContext(dispatcherProvider.io) {
        geocachingApiLoginUseCase(geocachingApi)

        geocachingApi.getBookmarkListByGuid(guid).map {
            BookmarkEntity(it.cacheCode(), it.cacheTitle(), it.geocacheType())
        }
    }
}