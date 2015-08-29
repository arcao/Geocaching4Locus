package com.arcao.geocaching4locus.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;

public final class AnalyticsUtil {
	public static final String COORDINATES_SOURCE_LOCUS = "LOCUS";
	public static final String COORDINATES_SOURCE_GPS = "GPS";
	public static final String COORDINATES_SOURCE_MANUAL = "MANUAL";

	public static void actionLogin(boolean success) {
		Answers.getInstance().logLogin(new LoginEvent().putSuccess(success));
	}

	public static void actionDashboard(boolean calledFromLocus) {
		Answers.getInstance().logCustom(new CustomEvent("Dashboard")
						.putCustomAttribute("called from locus", Boolean.toString(calledFromLocus)));
	}

	public static void actionImport() {
		Answers.getInstance().logCustom(new CustomEvent("Import"));
	}

	public static void actionImportBookmarks(int count, boolean all) {
		Answers.getInstance().logCustom(new CustomEvent("Import bookmarks")
						.putCustomAttribute("count", count).putCustomAttribute("all", Boolean.toString(all)));
	}

	public static void actionImportGC() {
		Answers.getInstance().logCustom(new CustomEvent("Import GC"));
	}

	public static void actionSearchNearest(String coordinatesSource, boolean useFilter, int count) {
		Answers.getInstance().logCustom(new CustomEvent("Search nearest")
						.putCustomAttribute("coordinates source", coordinatesSource == null ? COORDINATES_SOURCE_MANUAL : coordinatesSource)
						.putCustomAttribute("use filter", Boolean.toString(useFilter))
						.putCustomAttribute("count", count));

	}

	public static void actionUpdate(boolean oldPoint, boolean updateLogs) {
		Answers.getInstance().logCustom(new CustomEvent("Update")
						.putCustomAttribute("old point", Boolean.toString(oldPoint))
						.putCustomAttribute("update logs", Boolean.toString(updateLogs)));

	}

	public static void actionUpdateMore(int count) {
		Answers.getInstance().logCustom(new CustomEvent("Update More").putCustomAttribute("count", count));
	}
}
