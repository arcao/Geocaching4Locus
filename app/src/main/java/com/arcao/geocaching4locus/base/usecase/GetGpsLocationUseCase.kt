package com.arcao.geocaching4locus.base.usecase

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.getSystemService
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.hasGpsLocationPermission
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume

class GetGpsLocationUseCase(
    val context: Context,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) {
    private val locationManager = requireNotNull(context.getSystemService<LocationManager>())

    @SuppressLint("MissingPermission")
    suspend operator fun invoke() = withContext(dispatcherProvider.main) {
        suspendCancellableCoroutine<Location?> { result ->
            if (!context.hasGpsLocationPermission || !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                result.resume(null)
                return@suspendCancellableCoroutine
            }

            val listener = GpsLocationListener(result, locationManager)

            result.invokeOnCancellation {
                Timber.i("removeUpdates")
                locationManager.removeUpdates(listener)
                Timber.i("Location listener removed.")
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, listener)
            Timber.i("Location listener added.")
        }
    }

    private class GpsLocationListener(
        private val result: CancellableContinuation<Location?>,
        private val locationManager: LocationManager
    ) : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (result.isCompleted) return

            Timber.i("onLocationChanged")
            locationManager.removeUpdates(this)
            Timber.i("Location listener removed.")
            result.resume(location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
            if (result.isCompleted) return

            Timber.i("onProviderDisabled: $provider")

            locationManager.removeUpdates(this)
            Timber.i("Location listener removed.")
            result.resume(null)
        }
    }
}