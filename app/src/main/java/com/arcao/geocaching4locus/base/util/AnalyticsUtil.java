package com.arcao.geocaching4locus.base.util;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;

public final class AnalyticsUtil {
    public static final String COORDINATES_SOURCE_LOCUS = "LOCUS";
    public static final String COORDINATES_SOURCE_GPS = "GPS";
    public static final String COORDINATES_SOURCE_MANUAL = "MANUAL";

    public static void actionLogin(boolean success, boolean premiumMember) {
        Answers.getInstance().logLogin(new LoginEvent().putSuccess(success)
                .putCustomAttribute("premium member", Boolean.toString(premiumMember)));
    }

    public static void actionDashboard(boolean calledFromLocus) {
        Answers.getInstance().logCustom(new CustomEvent("Dashboard")
                .putCustomAttribute("called from locus", Boolean.toString(calledFromLocus)));
    }

    public static void actionImport(boolean premiumMember) {
        Answers.getInstance().logCustom(new CustomEvent("Import")
                .putCustomAttribute("premium member", Boolean.toString(premiumMember)));
    }

    public static void actionImportBookmarks(int count, boolean all) {
        Answers.getInstance().logCustom(new CustomEvent("Import bookmarks")
                .putCustomAttribute("count", count).putCustomAttribute("all", Boolean.toString(all)));
    }

    public static void actionImportGC(boolean premiumMember) {
        Answers.getInstance().logCustom(new CustomEvent("Import GC")
                .putCustomAttribute("premium member", Boolean.toString(premiumMember)));
    }

    public static void actionSearchNearest(String coordinatesSource, boolean useFilter, int count, boolean premiumMember) {
        Answers.getInstance().logCustom(new CustomEvent("Search nearest")
                .putCustomAttribute("coordinates source", coordinatesSource == null ? COORDINATES_SOURCE_MANUAL : coordinatesSource)
                .putCustomAttribute("use filter", Boolean.toString(useFilter))
                .putCustomAttribute("count", count)
                .putCustomAttribute("premium member", Boolean.toString(premiumMember)));
    }

    public static void actionUpdate(boolean oldPoint, boolean updateLogs, boolean premiumMember) {
        Answers.getInstance().logCustom(new CustomEvent("Update")
                .putCustomAttribute("old point", Boolean.toString(oldPoint))
                .putCustomAttribute("update logs", Boolean.toString(updateLogs))
                .putCustomAttribute("premium member", Boolean.toString(premiumMember)));
    }

    public static void actionUpdateMore(int count, boolean premiumMember) {
        Answers.getInstance().logCustom(new CustomEvent("Update More")
                .putCustomAttribute("count", count)
                .putCustomAttribute("premium member", Boolean.toString(premiumMember)));
    }
}
