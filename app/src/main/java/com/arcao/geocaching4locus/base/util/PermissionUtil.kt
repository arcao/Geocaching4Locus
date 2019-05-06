package com.arcao.geocaching4locus.base.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arcao.geocaching4locus.error.fragment.ExternalStoragePermissionWarningDialogFragment

object PermissionUtil {
    private const val REQUEST_PERMISSION_BASE = 100
    const val REQUEST_LOCATION_PERMISSION = REQUEST_PERMISSION_BASE + 1
    const val REQUEST_EXTERNAL_STORAGE_PERMISSION = REQUEST_PERMISSION_BASE + 2

    val PERMISSION_LOCATION_GPS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val PERMISSION_LOCATION_WIFI = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    val PERMISSION_EXTERNAL_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

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

    fun requestExternalStoragePermission(activity: AppCompatActivity): Boolean {
        return if (activity.hasExternalStoragePermission) {
            true
        } else {
            ExternalStoragePermissionWarningDialogFragment.newInstance().show(activity.supportFragmentManager)
            false
        }
    }

    fun requestGpsLocationPermission(activity: AppCompatActivity): Boolean {
        return if (activity.hasGpsLocationPermission) {
            true
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSION_LOCATION_GPS, REQUEST_LOCATION_PERMISSION)
            false
        }
    }

    fun requestWifiLocationPermission(activity: AppCompatActivity): Boolean {
        return if (activity.hasGpsLocationPermission) {
            true
        } else {
            ActivityCompat.requestPermissions(activity, PERMISSION_LOCATION_WIFI, REQUEST_LOCATION_PERMISSION)
            false
        }
    }
}

val Context.hasExternalStoragePermission
    get() = PermissionUtil.hasPermission(this, *PermissionUtil.PERMISSION_EXTERNAL_STORAGE)

val Context.hasGpsLocationPermission
    get() = PermissionUtil.hasPermission(this, *PermissionUtil.PERMISSION_LOCATION_GPS)

val Context.hasWifiLocationPermission
    get() = PermissionUtil.hasPermission(this, *PermissionUtil.PERMISSION_LOCATION_WIFI)