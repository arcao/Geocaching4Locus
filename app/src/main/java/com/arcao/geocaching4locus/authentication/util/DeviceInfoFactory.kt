package com.arcao.geocaching4locus.authentication.util

import android.os.Build
import com.arcao.geocaching.api.data.DeviceInfo
import com.arcao.geocaching4locus.App

class DeviceInfoFactory(private val app: App) {
    @JvmName("create")
    operator fun invoke(): DeviceInfo = DeviceInfo.builder()
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
