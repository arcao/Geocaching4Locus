package com.arcao.geocaching4locus.download_rectangle

import android.content.Context
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.live_map.model.LastLiveMapCoordinates
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.map
import locus.api.manager.LocusMapManager
import timber.log.Timber

@UseExperimental(ExperimentalCoroutinesApi::class)
class DownloadRectangleViewModel constructor(
    private val context: Context,
    private val accountManager: AccountManager,
    private val exceptionHandler: ExceptionHandler,
    private val getPointsFromRectangleCoordinates: GetPointsFromRectangleCoordinatesUseCase,
    private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
    private val filterPreferenceManager: FilterPreferenceManager,
    private val locusMapManager: LocusMapManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {

    val action = Command<DownloadRectangleAction>()

    fun startDownload() = mainLaunch {
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

    @Suppress("EXPERIMENTAL_API_USAGE")
    private suspend fun doDownload(liveMapCoordinates: LastLiveMapCoordinates) = computationContext {
        val downloadIntent = locusMapManager.createSendPointsIntent(
            callImport = true,
            center = true
        )

        var count = AppConstants.ITEMS_PER_REQUEST
        var receivedGeocaches = 0

        try {
            showProgress(R.string.progress_download_geocaches, maxProgress = count) {
                val geocaches = getPointsFromRectangleCoordinates(
                    this,
                    liveMapCoordinates.center,
                    liveMapCoordinates.topLeft,
                    liveMapCoordinates.bottomRight,
                    filterPreferenceManager.simpleCacheData,
                    false,
                    filterPreferenceManager.geocacheLogsCount,
                    filterPreferenceManager.trackableLogsCount,
                    filterPreferenceManager.showDisabled,
                    filterPreferenceManager.showFound,
                    filterPreferenceManager.showOwn,
                    filterPreferenceManager.geocacheTypes,
                    filterPreferenceManager.containerTypes,
                    filterPreferenceManager.difficultyMin,
                    filterPreferenceManager.difficultyMax,
                    filterPreferenceManager.terrainMin,
                    filterPreferenceManager.terrainMax,
                    filterPreferenceManager.excludeIgnoreList,
                    AppConstants.LIVEMAP_CACHES_COUNT
                ) { count = it }.map { list ->
                    receivedGeocaches += list.size
                    updateProgress(progress = receivedGeocaches, maxProgress = count)

                    // apply additional downloading full geocache if required
                    if (filterPreferenceManager.simpleCacheData) {
                        list.forEach { point ->
                            point.setExtraOnDisplay(
                                context.packageName,
                                UpdateActivity::class.java.name,
                                UpdateActivity.PARAM_SIMPLE_CACHE_ID,
                                point.gcData.cacheID
                            )
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
        job.cancel()
        action(DownloadRectangleAction.Cancel)
    }
}