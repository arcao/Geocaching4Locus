package com.arcao.geocaching4locus.base.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {
    val PERMISSION_LOCATION_GPS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val PERMISSION_LOCATION_WIFI = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)

    fun verifyPermissions(@NonNull grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    fun hasPermission(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED)
                return false
        }

        return true
    }

    fun requestGpsLocationPermission(activity: AppCompatActivity, requestCode : Int): Boolean {
        return if (activity.hasGpsLocationPermission) {
            true
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSION_LOCATION_GPS, requestCode)
            false
        }
    }

    fun requestWifiLocationPermission(activity: AppCompatActivity, requestCode : Int): Boolean {
        return if (activity.hasGpsLocationPermission) {
            true
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSION_LOCATION_WIFI, requestCode)
            false
        }
    }
}

val Context.hasGpsLocationPermission
    get() = PermissionUtil.hasPermission(this, *PermissionUtil.PERMISSION_LOCATION_GPS)

val Context.hasWifiLocationPermission
    get() = PermissionUtil.hasPermission(this, *PermissionUtil.PERMISSION_LOCATION_WIFI)