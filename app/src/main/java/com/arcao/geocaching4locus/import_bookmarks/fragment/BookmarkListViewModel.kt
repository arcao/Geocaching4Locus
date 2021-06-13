package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.paging.DataSourceState
import com.arcao.geocaching4locus.base.usecase.GetListGeocachesUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.import_bookmarks.paging.GeocacheUserListsDataSource
import com.arcao.geocaching4locus.import_bookmarks.paging.GeocacheUserListsDataSourceFactory
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import locus.api.manager.LocusMapManager
import timber.log.Timber

@Suppress("EXPERIMENTAL_API_USAGE")
class BookmarkListViewModel(
    private val context: Context,
    private val exceptionHandler: ExceptionHandler,
    private val dataSourceFactory: GeocacheUserListsDataSourceFactory,
    private val getListGeocaches: GetListGeocachesUseCase,
    private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
    private val filterPreferenceManager: FilterPreferenceManager,
    private val locusMapManager: LocusMapManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val loading = MutableLiveData<Boolean>()
    val pagerFlow: Flow<PagingData<GeocacheListEntity>>
    val action = Command<BookmarkListAction>()
    private var job: Job? = null

    val state: LiveData<DataSourceState>
        get() = Transformations.switchMap(
            dataSourceFactory.dataSource,
            GeocacheUserListsDataSource::state
        )

    init {
        val pageSize = 25
        val config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false,
            initialLoadSize = 2 * pageSize
        )

        pagerFlow = Pager(
            config,
            pagingSourceFactory = dataSourceFactory.asPagingSourceFactory(Dispatchers.IO)
        ).flow.cachedIn(viewModelScope)

        state.observeForever { state ->
            if (state == DataSourceState.LoadingInitial) {
                loading(true)
            } else {
                if (loading.value == true) {
                    loading(false)
                }
            }
            if (state is DataSourceState.Error) {
                action(BookmarkListAction.LoadingError(exceptionHandler(state.e)))
            }
        }
    }

    fun importAll(geocacheList: GeocacheListEntity) {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = computationLaunch {
            val importIntent = locusMapManager.createSendPointsIntent(
                callImport = true,
                center = true
            )

            var receivedGeocaches = 0

            try {
                showProgress(R.string.progress_download_geocaches) {
                    Timber.d("source: import_from_bookmark;guid=%s", geocacheList.guid)

                    var count = 0

                    val channel = getListGeocaches(
                        geocacheList.guid,
                        filterPreferenceManager.simpleCacheData,
                        filterPreferenceManager.geocacheLogsCount
                    ) {
                        count = it
                        Timber.d(
                            "source: import_from_bookmark; guid=%s; count=%d",
                            geocacheList.guid,
                            count
                        )
                    }.map { list ->
                        receivedGeocaches += list.size
                        updateProgress(progress = receivedGeocaches, maxProgress = count)

                        // apply additional downloading full geocache if required
                        if (filterPreferenceManager.simpleCacheData) {
                            list.forEach { point ->
                                point.gcData?.cacheID?.let { cacheId ->
                                    point.setExtraOnDisplay(
                                        context.packageName,
                                        UpdateActivity::class.java.name,
                                        UpdateActivity.PARAM_SIMPLE_CACHE_ID,
                                        cacheId
                                    )
                                }
                            }
                        }
                        list
                    }
                    writePointToPackPointsFile(channel)
                }
            } catch (e: Exception) {
                mainContext {
                    action(
                        BookmarkListAction.Error(
                            if (receivedGeocaches > 0) {
                                exceptionHandler(IntendedException(e, importIntent))
                            } else {
                                exceptionHandler(e)
                            }
                        )
                    )
                }
                return@computationLaunch
            }

            mainContext {
                action(BookmarkListAction.Finish(importIntent))
            }
        }
    }

    fun chooseBookmarks(geocacheList: GeocacheListEntity) {
        action(BookmarkListAction.ChooseBookmarks(geocacheList))
    }

    fun cancelProgress() {
        job?.cancel()
    }
}
