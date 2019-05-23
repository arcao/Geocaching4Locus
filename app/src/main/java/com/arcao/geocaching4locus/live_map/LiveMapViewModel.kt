package com.arcao.geocaching4locus.live_map

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetLiveMapPointsFromRectangleCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.RemoveLocusMapPointsUseCase
import com.arcao.geocaching4locus.base.usecase.SendPointsSilentToLocusMapUseCase
import com.arcao.geocaching4locus.base.util.getText
import com.arcao.geocaching4locus.data.api.exception.AuthenticationException
import com.arcao.geocaching4locus.data.api.exception.GeocachingApiException
import com.arcao.geocaching4locus.data.api.model.Coordinates
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateActivity
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.IOException

class LiveMapViewModel(
    private val context: Context,
    private val notificationManager: LiveMapNotificationManager,
    private val filterPreferenceManager: FilterPreferenceManager,
    private val defaultPreferenceManager: DefaultPreferenceManager,
    private val getLiveMapPointsFromRectangleCoordinates: GetLiveMapPointsFromRectangleCoordinatesUseCase,
    private val sendPointsSilentToLocusMap: SendPointsSilentToLocusMapUseCase,
    private val removeLocusMapPoints: RemoveLocusMapPointsUseCase,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider), LifecycleObserver {

    fun addTask(intent: Intent, completionCallback: (Intent) -> Unit) {
        cancelTasks()

        downloadLiveMapGeocaches(intent).invokeOnCompletion {
            completionCallback(intent)
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private fun downloadLiveMapGeocaches(task: Intent) = computationLaunch {
        var requests = 0

        delay(200)

        try {
            var count = AppConstants.LIVEMAP_CACHES_COUNT
            var receivedGeocaches = 0

            showProgress(maxProgress = count) {
                val pointListChannel = getLiveMapPointsFromRectangleCoordinates(
                    this,
                    task.getCoordinates(LiveMapService.PARAM_LATITUDE, LiveMapService.PARAM_LONGITUDE),
                    task.getCoordinates(
                        LiveMapService.PARAM_TOP_LEFT_LATITUDE,
                        LiveMapService.PARAM_TOP_LEFT_LONGITUDE
                    ),
                    task.getCoordinates(
                        LiveMapService.PARAM_BOTTOM_RIGHT_LATITUDE,
                        LiveMapService.PARAM_BOTTOM_RIGHT_LONGITUDE
                    ),
                    filterPreferenceManager.simpleCacheData,
                    filterPreferenceManager.showDisabled,
                    filterPreferenceManager.showFound,
                    filterPreferenceManager.showOwn,
                    filterPreferenceManager.geocacheTypes,
                    filterPreferenceManager.containerTypes,
                    filterPreferenceManager.difficultyMin,
                    filterPreferenceManager.difficultyMax,
                    filterPreferenceManager.terrainMin,
                    filterPreferenceManager.terrainMax,
                    filterPreferenceManager.excludeIgnoreList
                ) { count = it }.map { list ->
                    receivedGeocaches += list.size
                    requests++

                    updateProgress(progress = receivedGeocaches, maxProgress = count)

                    list.forEach { point ->
                        point.setExtraOnDisplay(
                            context.packageName,
                            UpdateActivity::class.java.name,
                            UpdateActivity.PARAM_SIMPLE_CACHE_ID,
                            point.gcData.cacheID
                        )
                    }
                    list
                }

                // send to locus map
                sendPointsSilentToLocusMap(AppConstants.LIVEMAP_PACK_WAYPOINT_PREFIX, pointListChannel)
            }
        } catch (e: Exception) {
            handleException(e)
        } finally {
            val lastRequests = defaultPreferenceManager.liveMapLastRequests
            if (requests < lastRequests) {
                removeLocusMapPoints(AppConstants.LIVEMAP_PACK_WAYPOINT_PREFIX, requests + 1, lastRequests)
            }
            defaultPreferenceManager.liveMapLastRequests = requests
        }
    }

    fun cancelTasks() {
        coroutineContext.cancelChildren()
    }

    private fun handleException(e: Exception) {
        Timber.e(e)

        when (e) {
            is LocusMapRuntimeException -> {
                notificationManager.showLiveMapToast(context.getText(R.string.error_locus_map, e.message ?: ""))
                // disable live map
                notificationManager.isLiveMapEnabled = false
            }
            is AuthenticationException -> {
                notificationManager.showLiveMapToast(R.string.error_no_account)
                // disable live map
                notificationManager.isLiveMapEnabled = false
            }
            is IOException -> {
                notificationManager.showLiveMapToast(R.string.error_network_unavailable)
            }
            is GeocachingApiException -> {
                notificationManager.showLiveMapToast(e.errorMessage.orEmpty())
            }
            else -> throw e
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    override fun onCleared() {
        super.onCleared()
    }
}

private fun Intent.getCoordinates(latitudeName: String, longitudeName: String) = Coordinates(
    getDoubleExtra(latitudeName, 0.0),
    getDoubleExtra(longitudeName, 0.0)
)

