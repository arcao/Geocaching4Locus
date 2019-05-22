package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkListEntity
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.withContext

class GetUserBookmarkListsUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(): List<BookmarkListEntity> = withContext(dispatcherProvider.io) {
        geocachingApiLogin()

        repository.userLists().map {
            BookmarkListEntity(
                it.id,
                it.referenceCode,
                it.name,
                it.description,
                it.count,
                it.isShared,
                it.isPublic,
                it.type
            )
        }
    }
}