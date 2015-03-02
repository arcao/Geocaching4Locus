package com.arcao.feedback.collector;

import android.content.Context;
import android.content.pm.PackageInfo;
import org.acra.util.PackageManagerWrapper;

/**
 * Created by msloup on 13.1.2015.
 */
public class AppInfoCollector extends Collector {
	private Context context;

	public AppInfoCollector(Context context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "APP INFO";
	}

	@Override
	protected String collect() {
		final StringBuilder sb = new StringBuilder();

		final PackageManagerWrapper pm = new PackageManagerWrapper(context);

		final PackageInfo pi = pm.getPackageInfo();
		if (pi != null) {
			sb.append("APP_PACKAGE=").append(pi.packageName).append("\n");
			sb.append("APP_VERSION_CODE=").append(pi.versionCode).append("\n");
			sb.append("APP_VERSION_NAME=").append(pi.versionName).append("\n");
		}

		return sb.toString();
	}
}
