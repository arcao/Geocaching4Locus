package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.model.enums.GeocacheListType
import com.arcao.geocaching4locus.data.api.model.response.PagedArrayList
import com.arcao.geocaching4locus.data.api.model.response.PagedList
import kotlinx.coroutines.withContext

class GetUserListsUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        referenceCode: String = "me",
        types: Set<GeocacheListType> = setOf(GeocacheListType.BOOKMARK),
        skip: Int = 0,
        take: Int = 10
    ): PagedList<GeocacheListEntity> = withContext(dispatcherProvider.io) {
        geocachingApiLogin()

        val list = repository.userLists(
            referenceCode, types, skip, take
        )

        list.mapTo(PagedArrayList(list.size, list.totalCount)) {
            GeocacheListEntity(
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