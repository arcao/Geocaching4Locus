package com.arcao.geocaching4locus.authentication.util;

import android.content.Context;
import android.os.Build;

import com.arcao.geocaching.api.data.DeviceInfo;
import com.arcao.geocaching4locus.App;

public final class DeviceInfoFactory {
	public static DeviceInfo create(Context context) {
		App app = App.get(context);

		return DeviceInfo.builder()
				.applicationCurrentMemoryUsage(0)
				.applicationPeakMemoryUsage(0)
				.applicationSoftwareVersion(app.getVersion())
				.deviceManufacturer(Build.MANUFACTURER)
				.deviceName(Build.MODEL)
				.deviceOperatingSystem(Build.VERSION.RELEASE)
				.deviceTotalMemoryInMb(0)
				.deviceUniqueId(app.getDeviceId())
				.build();
	}
}
