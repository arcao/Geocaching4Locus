package com.arcao.geocaching4locus.authentication.util

import android.content.Context
import android.os.Build

import com.arcao.geocaching.api.data.DeviceInfo
import com.arcao.geocaching4locus.App

object DeviceInfoFactory {
    // TODO change Context to App
    @JvmStatic
    fun create(context: Context): DeviceInfo {
        val app = if (context is App)
            context
        else
            context.applicationContext as App

        return DeviceInfo.builder()
                .applicationCurrentMemoryUsage(0)
                .applicationPeakMemoryUsage(0)
                .applicationSoftwareVersion(app.version)
                .deviceManufacturer(Build.MANUFACTURER)
                .deviceName(Build.MODEL)
                .deviceOperatingSystem(Build.VERSION.RELEASE)
                .deviceTotalMemoryInMb(0f)
                .deviceUniqueId(app.deviceId)
                .build()
    }
}
