package com.arcao.geocaching4locus.util;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import timber.log.Timber;

public final class CrashlyticsTree extends Timber.Tree {
	@Override
	protected void log(int priority, String tag, String message, Throwable t) {
		Crashlytics.log(priority, tag, t == null ? message : message + '\n' + Log.getStackTraceString(t));
	}
}