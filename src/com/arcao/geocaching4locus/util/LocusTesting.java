package com.arcao.geocaching4locus.util;

import java.util.List;

import locus.api.android.utils.LocusUtils;

import org.osgi.framework.Version;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.fragment.ErrorDialogFragment;

public class LocusTesting {
	private static final String TAG = "Geocaching4Locus|LocusTesting";
	
	private static final Version LOCUS_MIN_VERSION = Version.parseVersion("2.8.3.2");
	
	protected static final String GOOGLE_PLAY_PREFIX = "https://play.google.com/store/apps/details?id=";
	protected static final Uri ANDROIDPIT_LOCUS_FREE_LINK = Uri.parse("http://www.androidpit.com/en/android/market/apps/app/menion.android.locus/Locus-Free");
	protected static final Uri ANDROIDPIT_LOCUS_PRO_LINK = Uri.parse("http://www.androidpit.com/en/android/market/apps/app/menion.android.locus.pro/Locus-Pro");
	
	public static boolean isLocusInstalled(Context context) {
		PackageInfo pi = LocusUtils.getLocusPackageInfo(context);
		
		Version locusVersion = null;
		if (pi != null) {
			locusVersion = Version.parseVersion(pi.versionName);	
		} else {
			locusVersion = Version.parseVersion(null);
		}
		
		Log.i(TAG, "Locus version: " + locusVersion + "; Required version: " + LOCUS_MIN_VERSION);
		
		return locusVersion.compareTo(LOCUS_MIN_VERSION) >= 0;
	}
	
	public static void showLocusMissingError(final FragmentActivity activity) {
		showError(activity, LocusUtils.isLocusAvailable(activity) ? R.string.error_locus_old : R.string.error_locus_not_found, LOCUS_MIN_VERSION.toString(), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Uri localUri;
				if (isAndroidMarketInstalled(activity)) {
					// create Uri for Locus Free on Google Play
					localUri = Uri.parse(GOOGLE_PLAY_PREFIX + LocusUtils.LOCUS_PACKAGE_NAMES[1]);
				} else {
					if (LocusUtils.isLocusProAvailable(activity, LocusUtils.LOCUS_API_SINCE_VERSION)) {
						localUri = ANDROIDPIT_LOCUS_PRO_LINK;
					} else {
						localUri = ANDROIDPIT_LOCUS_FREE_LINK;
					}
				}
				Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
				activity.startActivity(localIntent);
				activity.finish();
			}
		});
	}
	
	protected static boolean isAndroidMarketInstalled(Context context) {
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
	
	public static void showError(FragmentActivity activity, int resErrorMessage, String additionalMessage, OnClickListener onClickListener) {
		if (activity.isFinishing())
			return;
		
		ErrorDialogFragment.newInstance(R.string.error_title, resErrorMessage, additionalMessage, onClickListener)
			.show(activity.getSupportFragmentManager(), ErrorDialogFragment.TAG);
	}
}
