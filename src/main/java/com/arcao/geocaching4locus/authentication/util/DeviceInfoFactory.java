package com.arcao.geocaching4locus.authentication.util;

import android.content.Context;
import android.os.Build;

import com.arcao.geocaching.api.data.DeviceInfo;
import com.arcao.geocaching4locus.App;

public final class DeviceInfoFactory {
	public static DeviceInfo create(Context context) {
		App app = App.get(context);

		return new DeviceInfo(
				0,
				0,
				app.getVersion(),
				Build.MANUFACTURER,
				Build.MODEL,
				Build.VERSION.RELEASE,
				0,
				app.getDeviceId(),
				null,
				null
		);
	}
}
