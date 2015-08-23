package com.arcao.geocaching4locus.util;

import com.crashlytics.android.Crashlytics;

import timber.log.Timber;

public final class CrashlyticsTree extends Timber.Tree {
	@Override
	protected void log(int priority, String tag, String message, Throwable t) {
		Crashlytics.log(priority, tag, message);
		if (t != null)
			Crashlytics.logException(t);
	}
}