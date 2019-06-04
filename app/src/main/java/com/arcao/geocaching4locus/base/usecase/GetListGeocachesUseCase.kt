package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.ListGeocacheEntity
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.model.response.PagedArrayList
import com.arcao.geocaching4locus.data.api.model.response.PagedList
import kotlinx.coroutines.withContext

class GetListGeocachesUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    suspend operator fun invoke(
        referenceCode: String,
        skip: Int = 0,
        take: Int = 10
    ): PagedList<ListGeocacheEntity> = withContext(dispatcherProvider.io) {
        geocachingApiLogin()

        val list = repository.listGeocaches(
            referenceCode = referenceCode,
            lite = true,
            skip = skip,
            take = take
        )
        list.mapTo(PagedArrayList(list.size, list.totalCount)) {
            ListGeocacheEntity(it.referenceCode, it.name, it.geocacheType.id)
        }
    }
}