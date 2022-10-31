package com.arcao.geocaching4locus.base.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtil {
    val PERMISSION_LOCATION_GPS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val PERMISSION_LOCATION_WIFI = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)

    fun hasPermission(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) return false
        }

        return true
    }
}

val Context.hasGpsLocationPermission
    get() = PermissionUtil.hasPermission(this, *PermissionUtil.PERMISSION_LOCATION_GPS)

val Context.hasWifiLocationPermission
    get() = PermissionUtil.hasPermission(this, *PermissionUtil.PERMISSION_LOCATION_WIFI)
