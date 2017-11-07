package com.arcao.geocaching4locus.base.util;

import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment;

import locus.api.android.utils.LocusUtils;
import timber.log.Timber;

public class LocusTesting {
    public static boolean isLocusInstalled(Context context) {
        LocusUtils.LocusVersion lv = LocusUtils.getActiveVersion(context);

        String locusVersion = lv != null ? lv.getVersionName() : "";

        Timber.v("Locus version: " + locusVersion + "; Required version: " + AppConstants.LOCUS_MIN_VERSION);

        return lv != null && lv.isVersionValid(AppConstants.LOCUS_MIN_VERSION_CODE);
    }

    public static void showLocusMissingError(final FragmentActivity activity) {
        LocusTestingErrorDialogFragment.newInstance(activity).show(activity.getFragmentManager(), LocusTestingErrorDialogFragment.FRAGMENT_TAG);
    }
}
