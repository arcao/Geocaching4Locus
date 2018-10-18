package com.arcao.geocaching4locus.base.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.arcao.geocaching4locus.error.fragment.ExternalStoragePermissionWarningDialogFragment

object PermissionUtil {
    private const val REQUEST_PERMISSION_BASE = 100
    const val REQUEST_LOCATION_PERMISSION = REQUEST_PERMISSION_BASE + 1
    const val REQUEST_EXTERNAL_STORAGE_PERMISSION = REQUEST_PERMISSION_BASE + 2

    @JvmField
    val PERMISSION_LOCATION_GPS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    @JvmField
    val PERMISSION_LOCATION_WIFI = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
    @JvmField
    val PERMISSION_EXTERNAL_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @JvmStatic
    fun verifyPermissions(@NonNull grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun hasPermission(context: Context, vararg permissions: String): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED)
                return false
        }

        return true
    }

    @JvmStatic
    fun requestExternalStoragePermission(@NonNull activity: AppCompatActivity): Boolean {
        return if (PermissionUtil.hasPermission(activity, *PERMISSION_EXTERNAL_STORAGE)) {
            true
        } else {
            ExternalStoragePermissionWarningDialogFragment.newInstance().show(activity.supportFragmentManager, ExternalStoragePermissionWarningDialogFragment.FRAGMENT_TAG)
            false
        }
    }
}
