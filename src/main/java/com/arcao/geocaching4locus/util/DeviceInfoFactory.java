package com.arcao.geocaching4locus.util;

import android.os.Build;

import com.arcao.geocaching.api.data.DeviceInfo;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;

public final class DeviceInfoFactory {
	public static DeviceInfo create() {
		return new DeviceInfo(
				0,
				0,
				Geocaching4LocusApplication.getVersion(),
				Build.MANUFACTURER,
				Build.MODEL,
				Build.VERSION.RELEASE,
				0,
				Geocaching4LocusApplication.getDeviceId(),
				null,
				null
		);
	}
}
