package com.arcao.geocaching4locus.base.usecase

import android.content.Context
import android.location.LocationManager
import androidx.core.content.getSystemService
import com.arcao.geocaching4locus.base.usecase.entity.LocationPermissionType
import com.arcao.geocaching4locus.base.util.hasGpsLocationPermission
import com.arcao.geocaching4locus.base.util.hasWifiLocationPermission

class RequireLocationPermissionRequestUseCase(
    private val context: Context
) {
    private val locationManager by lazy {
        requireNotNull(context.getSystemService<LocationManager>())
    }

    operator fun invoke(): LocationPermissionType = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !context.hasGpsLocationPermission -> {
            LocationPermissionType.GPS
        }
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) && !context.hasWifiLocationPermission -> {
            LocationPermissionType.WIFI
        }
        else -> LocationPermissionType.NONE
    }
}