package com.arcao.geocaching4locus.base.util;

import android.content.Context;

import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import locus.api.android.utils.LocusUtils;
import locus.api.objects.extra.Waypoint;
import timber.log.Timber;

public final class LocusMapUtil {
    private LocusMapUtil() {
    }

    @NonNull
    public static LocusUtils.LocusVersion getLocusVersion(Context context) {
        try {
            LocusUtils.LocusVersion locusVersion = LocusUtils.getActiveVersion(context);
            if (locusVersion == null)
                throw new IllegalStateException("Locus is not installed.");

            return locusVersion;
        } catch (Throwable t) {
            throw new LocusMapRuntimeException(t);
        }
    }

    public static boolean isLocusNotInstalled(Context context) {
        LocusUtils.LocusVersion lv = LocusUtils.getActiveVersion(context);

        String locusVersion = lv != null ? lv.getVersionName() : "";

        Timber.v("Locus version: " + locusVersion + "; Required version: " + AppConstants.LOCUS_MIN_VERSION);

        return lv == null || !lv.isVersionValid(AppConstants.LOCUS_MIN_VERSION_CODE);
    }

    public static void showLocusMissingError(final FragmentActivity activity) {
        LocusTestingErrorDialogFragment.newInstance(activity).show(activity.getFragmentManager(), LocusTestingErrorDialogFragment.FRAGMENT_TAG);
    }

    public static boolean isGeocache(Waypoint wpt) {
        return wpt == null || wpt.gcData == null || wpt.gcData.getCacheID() == null
                || !wpt.gcData.getCacheID().toUpperCase(Locale.US).startsWith("GC");
    }
}
