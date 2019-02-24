package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching.api.GeocachingApi
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import kotlinx.coroutines.withContext

class GetUserBookmarkListsUseCase(
    private val geocachingApi: GeocachingApi,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(): List<BookmarkListEntity> = withContext(dispatcherProvider.io) {
        geocachingApiLogin(geocachingApi)

        geocachingApi.bookmarkListsForUser.map {
            BookmarkListEntity(
                it.id(),
                it.guid(),
                it.name(),
                it.description(),
                it.itemCount(),
                it.shared(),
                it.publicList(),
                it.archived(),
                it.special(),
                it.type()
            )
        }
    }
}