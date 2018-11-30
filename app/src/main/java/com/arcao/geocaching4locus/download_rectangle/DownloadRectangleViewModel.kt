package com.arcao.geocaching4locus.download_rectangle

import android.content.Context
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.FilterPreferences
import com.arcao.geocaching4locus.base.util.hasExternalStoragePermission
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.isLocusNotInstalled
import com.arcao.geocaching4locus.download_rectangle.usecase.GetPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.live_map.model.LastLiveMapCoordinates
import com.arcao.geocaching4locus.update.UpdateActivity
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.launch
import locus.api.android.ActionDisplayPointsExtended
import timber.log.Timber

/**
 * Created by Arcao on 29.11.2018.
 */
class DownloadRectangleViewModel(
    private val context: Context,
    private val accountManager: AccountManager,
    private val exceptionHandler: ExceptionHandler,
    private val getPointsFromRectangleCoordinatesUseCase: GetPointsFromRectangleCoordinatesUseCase,
    private val writePointToPackPointsFileUseCase: WritePointToPackPointsFileUseCase,
    private val filterPreferences: FilterPreferences,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {

    val action = Command<DownloadRectangleAction>()

    fun startDownload() = launch {
        if (context.isLocusNotInstalled()) {
            mainContext {
                action(DownloadRectangleAction.LocusMapNotInstalled)
            }
            return@launch
        }

        if (accountManager.account == null) {
            mainContext {
                action(DownloadRectangleAction.SignIn)
            }
            return@launch
        }

        if (!context.hasExternalStoragePermission) {
            mainContext {
                action(DownloadRectangleAction.RequestExternalStoragePermission)
            }
            return@launch
        }

        val liveMapCoordinates = LastLiveMapCoordinates.value
        if (liveMapCoordinates == null) {
            mainContext {
                action(DownloadRectangleAction.LastLiveMapDataInvalid)
            }
            return@launch
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
    private suspend fun doDownload(liveMapCoordinates: LastLiveMapCoordinates) {
        val downloadIntent = ActionDisplayPointsExtended.createSendPacksIntent(
            ActionDisplayPointsExtended.cacheFileName,
            true,
            true
        )

        var count = AppConstants.ITEMS_PER_REQUEST
        var receivedGeocaches = 0

        try {
            showProgress(R.string.progress_download_geocaches, maxProgress = count) {
                val geocaches = getPointsFromRectangleCoordinatesUseCase(
                    liveMapCoordinates.center,
                    liveMapCoordinates.topLeft,
                    liveMapCoordinates.bottomRight,
                    filterPreferences.simpleCacheData,
                    filterPreferences.geocacheLogsCount,
                    filterPreferences.trackableLogsCount,
                    filterPreferences.showDisabled,
                    filterPreferences.showFound,
                    filterPreferences.showOwn,
                    filterPreferences.geocacheTypes,
                    filterPreferences.containerTypes,
                    filterPreferences.difficultyMin,
                    filterPreferences.difficultyMax,
                    filterPreferences.terrainMin,
                    filterPreferences.terrainMax,
                    filterPreferences.excludeIgnoreList
                ) { count = it }.map { list ->
                    receivedGeocaches += list.size
                    updateProgress(progress = receivedGeocaches, maxProgress = count)

                    // apply additional downloading full geocache if required
                    if (filterPreferences.simpleCacheData) {
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
                writePointToPackPointsFileUseCase(geocaches)
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
            return
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