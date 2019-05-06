package com.arcao.geocaching4locus.base.usecase

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.arcao.geocaching4locus.base.util.hasGpsLocationPermission
import com.arcao.geocaching4locus.base.util.hasWifiLocationPermission
import com.arcao.geocaching4locus.base.util.whenNaN
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import timber.log.Timber

class GetLastKnownLocationUseCase(
    private val context: Context,
    private val preferenceManager: DefaultPreferenceManager
) {
    private val locationManager = requireNotNull(context.getSystemService<LocationManager>())

    @SuppressLint("MissingPermission")
    operator fun invoke(): Location {
        var gpsLocation: Location? = null
        var networkLocation: Location? = null

        if (context.hasGpsLocationPermission) {
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        if (context.hasWifiLocationPermission) {
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        val location = when {
            gpsLocation == null && networkLocation != null -> networkLocation
            gpsLocation != null && networkLocation == null -> gpsLocation
            gpsLocation != null && networkLocation != null -> {
                if (networkLocation.time < gpsLocation.time) gpsLocation else networkLocation
            }
            else -> Location(LocationManager.PASSIVE_PROVIDER).apply {
                latitude = preferenceManager.lastLatitude.whenNaN(0.0)
                longitude = preferenceManager.lastLongitude.whenNaN(0.0)
            }
        }

        Timber.i("Last location found for: %s", location)

        return location
    }
}