package com.arcao.geocaching4locus.search_nearest

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetGpsLocationUseCase
import com.arcao.geocaching4locus.base.usecase.GetLastKnownLocationUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.GetWifiLocationUseCase
import com.arcao.geocaching4locus.base.usecase.RequireLocationPermissionRequestUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.usecase.entity.LocationPermissionType
import com.arcao.geocaching4locus.base.util.AnalyticsUtil
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.CoordinatesFormatter
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.model.Coordinates
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import com.arcao.geocaching4locus.update.UpdateActivity
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.map
import locus.api.manager.LocusMapManager
import timber.log.Timber

class SearchNearestViewModel(
    intent: Intent,
    private val context: Context,
    private val accountManager: AccountManager,
    private val preferenceManager: DefaultPreferenceManager,
    private val filterPreferenceManager: FilterPreferenceManager,
    private val locusMapManager: LocusMapManager,
    private val getGpsLocation: GetGpsLocationUseCase,
    private val getWifiLocation: GetWifiLocationUseCase,
    private val getLastKnownLocation: GetLastKnownLocationUseCase,
    private val requireLocationPermissionRequest: RequireLocationPermissionRequestUseCase,
    private val getPointsFromCoordinates: GetPointsFromCoordinatesUseCase,
    private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
    private val exceptionHandler: ExceptionHandler,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<SearchNearestAction>()

    val latitude = MutableLiveData<CharSequence>()
    val longitude = MutableLiveData<CharSequence>()
    val requestedCaches = MutableLiveData<Int>().apply {
        value = preferenceManager.downloadingGeocachesCount

        observeForever { value ->
            preferenceManager.downloadingGeocachesCount = value
        }
    }
    val requestedCachesMax = DefaultPreferenceManager.MAX_GEOCACHES_COUNT
    val requestedCachesStep = preferenceManager.downloadingGeocachesCountStep

    private var coordinatesSource = AnalyticsUtil.COORDINATES_SOURCE_MANUAL
    private var useFilter = false

    init {
        run {
            formatCoordinates(preferenceManager.lastLatitude, preferenceManager.lastLongitude)

            if (locusMapManager.isLocusMapNotInstalled) {
                action(SearchNearestAction.LocusMapNotInstalled)
                return@run
            }

            if (locusMapManager.isIntentPointTools(intent)) {
                locusMapManager.getPointFromIntent(intent)?.let { point ->
                    coordinatesSource = AnalyticsUtil.COORDINATES_SOURCE_LOCUS
                    formatCoordinates(point.location.latitude, point.location.longitude)
                }
            } else if (locusMapManager.isLocationIntent(intent)) {
                locusMapManager.getLocationFromIntent(intent)?.let { location ->
                    coordinatesSource = AnalyticsUtil.COORDINATES_SOURCE_LOCUS
                    formatCoordinates(location.latitude, location.longitude)
                }
            }

            val hasCoordinates = latitude.value != null && longitude.value != null
            if (!hasCoordinates) {
                retrieveCoordinates()
            }
        }
    }

    fun retrieveCoordinates() {
        // check permission
        when (requireLocationPermissionRequest()) {
            LocationPermissionType.GPS -> {
                action(SearchNearestAction.RequestGpsLocationPermission)
                return
            }
            LocationPermissionType.WIFI -> {
                action(SearchNearestAction.RequestWifiLocationPermission)
                return
            }
            LocationPermissionType.NONE -> {
                // continue
            }
        }

        coordinatesSource = AnalyticsUtil.COORDINATES_SOURCE_GPS

        mainLaunch {
            showProgress(R.string.progress_acquire_gps_location) {
                var location = getGpsLocation()

                if (location == null) {
                    updateProgress(R.string.progress_acquire_network_location)
                    location = getWifiLocation()
                }

                if (location == null) {
                    location = getLastKnownLocation()
                }

                if (location == null) {
                    val hasCoordinates = latitude.value != null && longitude.value != null
                    if (!hasCoordinates) {
                        formatCoordinates(0.0, 0.0)
                    }
                    action(SearchNearestAction.LocationProviderDisabled)
                    return@showProgress
                }

                formatCoordinates(location.latitude, location.longitude)
            }
        }
    }

    fun cancelProgress() {
        job.cancelChildren()
    }

    fun download() {
        formatCoordinates()

        mainLaunch {
            val latitude = CoordinatesFormatter.convertDegToDouble(latitude.value ?: "")
            val longitude = CoordinatesFormatter.convertDegToDouble(longitude.value ?: "")

            if (latitude.isNaN() || longitude.isNaN()) {
                action(SearchNearestAction.WrongCoordinatesFormat)
                return@mainLaunch
            }

            Timber.i("Lat: $latitude; Lon: $longitude")

            if (locusMapManager.isLocusMapNotInstalled) {
                action(SearchNearestAction.LocusMapNotInstalled)
                return@mainLaunch
            }

            if (accountManager.account == null) {
                action(SearchNearestAction.SignIn)
                return@mainLaunch
            }

            preferenceManager.lastLatitude = latitude
            preferenceManager.lastLongitude = longitude

            doDownload(Coordinates(latitude, longitude), requireNotNull(requestedCaches.value))
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private suspend fun doDownload(coordinates: Coordinates, maxCount: Int) = computationContext {
        AnalyticsUtil.actionSearchNearest(coordinatesSource, useFilter, maxCount, accountManager.isPremium)

        val downloadIntent = locusMapManager.createSendPointsIntent(
            callImport = true,
            center = false
        )

        var count = maxCount
        var receivedGeocaches = 0

        try {
            showProgress(R.string.progress_download_geocaches, maxProgress = count) {
                val points = getPointsFromCoordinates(
                    this,
                    coordinates,
                    preferenceManager.downloadDistanceMeters,
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
                    filterPreferenceManager.excludeIgnoreList,
                    maxCount
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

                writePointToPackPointsFile(points)
            }
        } catch (e: Exception) {
            mainContext {
                action(
                    SearchNearestAction.Error(
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
            action(SearchNearestAction.Finish(downloadIntent))
        }
    }

    private fun formatCoordinates() {
        latitude.value?.let {
            latitude(CoordinatesFormatter.convertDoubleToDeg(CoordinatesFormatter.convertDegToDouble(it), false))
        }
        longitude.value?.let {
            longitude(CoordinatesFormatter.convertDoubleToDeg(CoordinatesFormatter.convertDegToDouble(it), true))
        }
    }

    private fun formatCoordinates(latitude: Double, longitude: Double) {
        if (latitude.isNaN() || longitude.isNaN())
            return

        this.latitude(CoordinatesFormatter.convertDoubleToDeg(latitude, false))
        this.longitude(CoordinatesFormatter.convertDoubleToDeg(longitude, true))
    }

    fun showFilters() {
        useFilter = true
        action(SearchNearestAction.ShowFilters)
    }
}
