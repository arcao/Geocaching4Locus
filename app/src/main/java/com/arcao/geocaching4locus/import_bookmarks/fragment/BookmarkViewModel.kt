package com.arcao.geocaching4locus.import_bookmarks.fragment

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetListGeocachesUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.usecase.entity.GeocacheListEntity
import com.arcao.geocaching4locus.base.usecase.entity.ListGeocacheEntity
import com.arcao.geocaching4locus.base.util.AnalyticsManager
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.import_bookmarks.paging.ListGeocachesDataSource
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import locus.api.manager.LocusMapManager
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
class BookmarkViewModel(
    geocacheList: GeocacheListEntity,
    private val getListGeocaches: GetListGeocachesUseCase,
    private val context: Context,
    private val exceptionHandler: ExceptionHandler,
    private val getPointsFromGeocacheCodes: GetPointsFromGeocacheCodesUseCase,
    private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
    private val filterPreferenceManager: FilterPreferenceManager,
    private val locusMapManager: LocusMapManager,
    private val analyticsManager: AnalyticsManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {

    val pagerFlow: Flow<PagingData<ListGeocacheEntity>>
    val selection = MutableLiveData<List<ListGeocacheEntity>>().apply {
        value = emptyList()
    }
    val action = Command<BookmarkAction>()

    private var job: Job? = null

    init {
        val pageSize = 25
        val config = PagingConfig(
            pageSize = pageSize,
            enablePlaceholders = false,
            initialLoadSize = 2 * pageSize
        )

        pagerFlow = Pager(
            config,
            pagingSourceFactory = {
                ListGeocachesDataSource(
                    geocacheList.guid,
                    getListGeocaches,
                    config
                )
            }
        ).flow.cachedIn(viewModelScope)
    }

    fun download() {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = mainLaunch {
            val selection = selection.value ?: return@mainLaunch
            analyticsManager.actionImportBookmarks(selection.size, false)

            computationLaunch {
                val geocacheCodes = selection.map { it.referenceCode }.toTypedArray()
                Timber.d("source: import_from_bookmark;gccodes=%s", geocacheCodes)

                val importIntent = locusMapManager.createSendPointsIntent(
                    callImport = true,
                    center = true
                )

                var receivedGeocaches = 0

                try {
                    showProgress(
                        R.string.progress_download_geocaches,
                        maxProgress = geocacheCodes.size
                    ) {
                        val channel = getPointsFromGeocacheCodes(
                            geocacheCodes,
                            filterPreferenceManager.simpleCacheData,
                            filterPreferenceManager.geocacheLogsCount
                        ).map { list ->
                            receivedGeocaches += list.size
                            updateProgress(
                                progress = receivedGeocaches,
                                maxProgress = geocacheCodes.size
                            )

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
                            BookmarkAction.Error(
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
                    action(BookmarkAction.Finish(importIntent))
                }
            }
        }
    }

    fun cancelProgress() {
        job?.cancel()
    }

    fun handleLoadError(e: Throwable) {
        action(BookmarkAction.LoadingError(exceptionHandler(e)))
    }
}
