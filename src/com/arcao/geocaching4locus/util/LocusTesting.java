package com.arcao.geocaching4locus.util;

import java.util.List;

import locus.api.android.utils.LocusUtils;

import org.osgi.framework.Version;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.fragment.LocusTestingErrorDialogFragment;

public class LocusTesting {
	private static final String TAG = LocusTesting.class.getName();
		
	public static boolean isLocusInstalled(Context context) {
		PackageInfo pi = LocusUtils.getLocusPackageInfo(context);
		
		Version locusVersion = null;
		if (pi != null) {
			locusVersion = Version.parseVersion(pi.versionName);	
		} else {
			locusVersion = Version.parseVersion(null);
		}
		
		Log.i(TAG, "Locus version: " + locusVersion + "; Required version: " + AppConstants.LOCUS_MIN_VERSION);
		
		return locusVersion.compareTo(AppConstants.LOCUS_MIN_VERSION) >= 0;
	}
	
	public static void showLocusMissingError(final FragmentActivity activity) {
		if (activity.getSupportFragmentManager().findFragmentByTag(LocusTestingErrorDialogFragment.TAG) != null)
			return;
		
		LocusTestingErrorDialogFragment.newInstance().show(activity.getSupportFragmentManager(), LocusTestingErrorDialogFragment.TAG);
	}
	
	public static boolean isAndroidMarketInstalled(Context context) {
    Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=dummy"));
    PackageManager manager = context.getPackageManager();
    List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

    if (list != null && list.size() > 0) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).activityInfo.packageName.startsWith("com.android.vending") == true) {
                return true;
            }
        }
     }
    return false;
	}	
}
