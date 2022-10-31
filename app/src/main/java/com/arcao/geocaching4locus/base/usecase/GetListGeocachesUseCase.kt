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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend operator fun invoke(
        referenceCode: String,
        skip: Int = 0,
        take: Int = 10
    ): PagedList<ListGeocacheEntity> = withContext(dispatcherProvider.io) {
        geocachingApiLogin()

        val list = repository.listGeocaches(
            referenceCode = referenceCode,
            skip = skip,
            take = take
        )

        accountManager.restrictions().updateLimits(repository.userLimits())

        list.mapTo(PagedArrayList(list.size, list.totalCount)) {
            ListGeocacheEntity(it.referenceCode, it.name, it.geocacheType?.id ?: 0)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend operator fun invoke(
        referenceCode: String,
        liteData: Boolean = true,
        geocacheLogsCount: Int = 0,
        countHandler: (Int) -> Unit = {}
    ) = flow {
        geocachingApiLogin()

        var count = AppConstants.INITIAL_REQUEST_SIZE
        var current = 0

        var itemsPerRequest = AppConstants.INITIAL_REQUEST_SIZE
        while (current < count) {
            val startTimeMillis = System.currentTimeMillis()

            val geocaches = repository.listGeocachesDownload(
                referenceCode = referenceCode,
                lite = liteData,
                logsCount = geocacheLogsCount,
                skip = current,
                take = min(itemsPerRequest, count - current)
            ).also {
                count = it.totalCount.toInt()
                countHandler(count)
            }

            accountManager.restrictions().updateLimits(repository.userLimits())

            yield()

            if (geocaches.isEmpty())
                break

            emit(mapper.createLocusPoints(geocaches))
            current += geocaches.size

            itemsPerRequest = DownloadingUtil.computeRequestSize(
                itemsPerRequest,
                AppConstants.LIST_GEOCACHES_DOWNLOAD_MAX_ITEMS,
                startTimeMillis
            )
        }

        Timber.v("found geocaches: %d", current)

        if (current == 0) {
            throw NoResultFoundException()
        }
    }.flowOn(dispatcherProvider.io)
}
