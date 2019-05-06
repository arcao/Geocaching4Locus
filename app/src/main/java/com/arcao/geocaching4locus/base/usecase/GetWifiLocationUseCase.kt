package com.arcao.geocaching4locus.base.usecase

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.getSystemService
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.hasWifiLocationPermission
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume

class GetWifiLocationUseCase(
    val context: Context,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    private val locationManager = requireNotNull(context.getSystemService<LocationManager>())

    @SuppressLint("MissingPermission")
    suspend operator fun invoke() = withContext(dispatcherProvider.computation) {
        suspendCancellableCoroutine<Location?> { result ->
            if (!context.hasWifiLocationPermission || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                result.resume(null)
                return@suspendCancellableCoroutine
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    Timber.i("Location listener removed.")
                    result.resume(location)
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
                }

                override fun onProviderEnabled(provider: String?) {
                }

                override fun onProviderDisabled(provider: String) {
                    Timber.i("onProviderDisabled: $provider")

                    locationManager.removeUpdates(this)
                    Timber.i("Location listener removed.")
                    result.resume(null)
                }
            }

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, listener)
            Timber.i("Location listener added.")

            result.invokeOnCancellation {
                locationManager.removeUpdates(listener)
                Timber.i("Location listener removed.")
            }
        }
    }
}