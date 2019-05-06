package com.arcao.geocaching4locus.search_nearest

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.arcao.geocaching.api.data.coordinates.Coordinates
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetGpsLocationUseCase
import com.arcao.geocaching4locus.base.usecase.GetLastKnownLocationUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromCoordinatesUseCase
import com.arcao.geocaching4locus.base.usecase.GetWifiLocationUseCase
import com.arcao.geocaching4locus.base.usecase.RequireLocationPermissionRequestUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.usecase.entity.LocationPermissionType
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.CoordinatesFormatter
import com.arcao.geocaching4locus.base.util.hasExternalStoragePermission
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.isLocusNotInstalled
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

    init {
        run {
            formatCoordinates(preferenceManager.lastLatitude, preferenceManager.lastLongitude)

            if (locusMapManager.isLocusMapNotInstalled()) {
                action(SearchNearestAction.LocusMapNotInstalled)
                return@run
            }

            if (locusMapManager.isIntentPointTools(intent)) {
                locusMapManager.getPointFromIntent(intent)?.let { point ->
                    formatCoordinates(point.location.latitude, point.location.longitude)
                }
            } else if (locusMapManager.isLocationIntent(intent)) {
                locusMapManager.getLocationFromIntent(intent)?.let { location ->
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


        mainLaunch {
            showProgress(message = R.string.progress_acquire_gps_location) {
                var location = getGpsLocation()

                if (location == null) {
                    updateProgress(message = R.string.progress_acquire_network_location)
                    location = getWifiLocation()
                }

                if (location == null) {
                    location = getLastKnownLocation()
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


            if (context.isLocusNotInstalled()) {
                action(SearchNearestAction.LocusMapNotInstalled)
                return@mainLaunch
            }

            if (accountManager.account == null) {
                action(SearchNearestAction.SignIn)
                return@mainLaunch
            }

            if (!context.hasExternalStoragePermission) {
                action(SearchNearestAction.RequestExternalStoragePermission)
                return@mainLaunch
            }

            doDownload(Coordinates.create(latitude, longitude), requireNotNull(requestedCaches.value))
        }
    }

    @Suppress("EXPERIMENTAL_API_USAGE")
    private suspend fun doDownload(coordinates: Coordinates, maxCount: Int) = computationContext {
        val downloadIntent = LocusMapManager.createSendPointsIntent(
            callImport = true,
            center = true
        )

        var count = maxCount
        var receivedGeocaches = 0

        try {
            showProgress(R.string.progress_download_geocaches, maxProgress = count) {
                val geocaches = getPointsFromCoordinates(
                    coordinates,
                    preferenceManager.downloadDistanceMeters,
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
        action(SearchNearestAction.ShowFilters)
    }
}