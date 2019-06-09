package com.arcao.geocaching4locus.base.usecase

import com.arcao.geocaching4locus.authentication.util.restrictions
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.entity.ListGeocacheEntity
import com.arcao.geocaching4locus.base.util.DownloadingUtil
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.GeocachingApiRepository
import com.arcao.geocaching4locus.data.api.model.response.PagedArrayList
import com.arcao.geocaching4locus.data.api.model.response.PagedList
import com.arcao.geocaching4locus.error.exception.NoResultFoundException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import locus.api.mapper.DataMapper
import timber.log.Timber
import kotlin.math.min

class GetListGeocachesUseCase(
    private val repository: GeocachingApiRepository,
    private val geocachingApiLogin: GeocachingApiLoginUseCase,
    private val accountManager: AccountManager,
    private val mapper: DataMapper,
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

        accountManager.restrictions().updateLimits(repository.userLimits())

        list.mapTo(PagedArrayList(list.size, list.totalCount)) {
            ListGeocacheEntity(it.referenceCode, it.name, it.geocacheType?.id ?: 0)
        }
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(
        scope: CoroutineScope,
        referenceCode: String,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0,
        countHandler: (Int) -> Unit = {}
    ) = scope.produce(dispatcherProvider.io, capacity = 50) {
        geocachingApiLogin()

        var count = AppConstants.ITEMS_PER_REQUEST
        var current = 0

        var itemsPerRequest = AppConstants.ITEMS_PER_REQUEST
        while (current < count) {
            val startTimeMillis = System.currentTimeMillis()

            val geocaches = repository.listGeocaches(
                referenceCode = referenceCode,
                lite = liteData,
                logsCount = geocacheLogsCount,
                skip = current,
                take = min(itemsPerRequest, count - current)
            ).also {
                count = it.totalCount.toInt()
                withContext(dispatcherProvider.computation) {
                    countHandler(count)
                }
            }

            accountManager.restrictions().updateLimits(repository.userLimits())

            yield()

            if (geocaches.isEmpty())
                break

            send(mapper.createLocusPoints(geocaches))
            current += geocaches.size

            itemsPerRequest = DownloadingUtil.computeItemsPerRequest(itemsPerRequest, startTimeMillis)
        }

        Timber.v("found geocaches: %d", current)

        if (current == 0) {
            throw NoResultFoundException()
        }
    }
}
