package com.arcao.geocaching4locus.util;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.dialog.LocusTestingErrorDialogFragment;
import locus.api.android.utils.LocusUtils;
import org.osgi.framework.Version;
import timber.log.Timber;

public class LocusTesting {
	public static boolean isLocusInstalled(Context context) {
		LocusUtils.LocusVersion lv = getActiveVersion(context);

		Version locusVersion = Version.parseVersion(lv != null ? lv.versionName : null);
		Timber.v("Locus version: " + locusVersion + "; Required version: " + AppConstants.LOCUS_MIN_VERSION);

		return locusVersion.compareTo(AppConstants.LOCUS_MIN_VERSION) >= 0;
	}

	public static void showLocusMissingError(final FragmentActivity activity) {
		if (activity.getFragmentManager().findFragmentByTag(LocusTestingErrorDialogFragment.FRAGMENT_TAG) != null)
			return;

		LocusTestingErrorDialogFragment.newInstance(activity).show(activity.getFragmentManager(), LocusTestingErrorDialogFragment.FRAGMENT_TAG);
	}

	public static void showLocusTooOldToast(final Context context) {
		Toast.makeText(context, context.getString(R.string.error_locus_old, AppConstants.LOCUS_MIN_VERSION.toString()), Toast.LENGTH_LONG).show();
	}

	public static LocusUtils.LocusVersion getActiveVersion(Context context) {
		try {
			return LocusUtils.getActiveVersion(context);
		} catch (Throwable t) {
			Timber.e(t, t.getMessage());
			return LocusUtils.createLocusVersion(context);
		}
	}
}
