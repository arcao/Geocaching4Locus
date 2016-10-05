package com.arcao.geocaching4locus.base.util;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment;
import locus.api.android.utils.LocusUtils;
import org.osgi.framework.Version;
import timber.log.Timber;

public class LocusTesting {
	public static boolean isLocusInstalled(Context context) {
		LocusUtils.LocusVersion lv = getActiveVersion(context);

		Version locusVersion = Version.parseVersion(lv != null ? lv.getVersionName() : null);
		Timber.v("Locus version: " + locusVersion + "; Required version: " + AppConstants.LOCUS_MIN_VERSION);

		return locusVersion.compareTo(AppConstants.LOCUS_MIN_VERSION) >= 0;
	}

	public static void showLocusMissingError(final FragmentActivity activity) {
		LocusTestingErrorDialogFragment.newInstance(activity).show(activity.getFragmentManager(), LocusTestingErrorDialogFragment.FRAGMENT_TAG);
	}

	public static void showLocusTooOldToast(final Context context) {
		Toast.makeText(context, ResourcesUtil.getText(context, R.string.error_old_locus_map, AppConstants.LOCUS_MIN_VERSION.toString()), Toast.LENGTH_LONG).show();
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
