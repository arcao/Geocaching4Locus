package com.arcao.geocaching4locus.download_rectangle

import android.app.Application
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.live_map.model.LastLiveMapCoordinates
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import locus.api.manager.LocusMapManager
import timber.log.Timber

class DownloadRectangleViewModel constructor(
    private val context: Application,
    private val accountManager: AccountManager,
    private val exceptionHandler: ExceptionHandler,
    private val getPointsFromRectangleCoordinates: GetPointsFromRectangleCoordinatesUseCase,
    private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
    private val filterPreferenceManager: FilterPreferenceManager,
    private val locusMapManager: LocusMapManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<DownloadRectangleAction>()
    private var job: Job? = null

    fun startDownload() {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = mainLaunch {
            if (locusMapManager.isLocusMapNotInstalled) {
                action(DownloadRectangleAction.LocusMapNotInstalled)
                return@mainLaunch
            }

            if (accountManager.account == null) {
                action(DownloadRectangleAction.SignIn)
                return@mainLaunch
            }

            val liveMapCoordinates = LastLiveMapCoordinates.value
            if (liveMapCoordinates == null) {
                action(DownloadRectangleAction.LastLiveMapDataInvalid)
                return@mainLaunch
            }

            Timber.i(
                "source=download_rectangle;center=%s;topLeft=%s;bottomRight=%s",
                liveMapCoordinates.center,
                liveMapCoordinates.topLeft,
                liveMapCoordinates.bottomRight
            )

            doDownload(liveMapCoordinates)
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private suspend fun doDownload(liveMapCoordinates: LastLiveMapCoordinates) =
        computationContext {
            val downloadIntent = locusMapManager.createSendPointsIntent(
                callImport = true,
                center = true
            )

            var count = AppConstants.INITIAL_REQUEST_SIZE
            var receivedGeocaches = 0

            try {
                showProgress(R.string.progress_download_geocaches, maxProgress = count) {
                    val geocaches = getPointsFromRectangleCoordinates(
                        liveMapCoordinates.center,
                        liveMapCoordinates.topLeft,
                        liveMapCoordinates.bottomRight,
                        filterPreferenceManager.simpleCacheData,
                        filterPreferenceManager.geocacheLogsCount,
                        filterPreferenceManager.showDisabled,
                        filterPreferenceManager.showFound,
                        filterPreferenceManager.showOwn,
                        filterPreferenceManager.geocacheTypes,
                        filterPreferenceManager.containerTypes,
                        filterPreferenceManager.difficultyMin,
                        filterPreferenceManager.difficultyMax,
                        filterPreferenceManager.terrainMin,
                        filterPreferenceManager.terrainMax,
                        AppConstants.LIVEMAP_CACHES_COUNT
                    ) { count = it }.map { list ->
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
                    writePointToPackPointsFile(geocaches)
                }
            } catch (e: Exception) {
                mainContext {
                    action(
                        DownloadRectangleAction.Error(
                            if (receivedGeocaches > 0) {
                                exceptionHandler(IntendedException(e, downloadIntent))
                            } else {
                                exceptionHandler(e)
                            }
                        )
                    )
                }
                return@computationContext
            }

            mainContext {
                action(DownloadRectangleAction.Finish(downloadIntent))
            }

        }

    fun cancelDownload() {
        job?.cancel()
        action(DownloadRectangleAction.Cancel)
    }
}
