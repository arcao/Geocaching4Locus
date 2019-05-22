package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.BookmarkEntity
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import kotlinx.coroutines.withContext

class GetBookmarkUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        referenceCode: String,
        start: Int = 0,
        count: Int = 10
    ): List<BookmarkEntity> = withContext(dispatcherProvider.io) {
        geocachingApiLogin()

        repository.listGeocaches(
            referenceCode = referenceCode,
            lite = true,
            skip = start,
            take = count
        ).map {
            BookmarkEntity(it.referenceCode, it.name, it.geocacheType.id)
        }
    }
}