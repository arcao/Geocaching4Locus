package com.arcao.geocaching4locus.util;

import menion.android.locus.addon.publiclib.LocusUtils;

import org.osgi.framework.Version;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;

import com.arcao.geocaching4locus.R;

public class LocusTesting {
	private static final String TAG = "Geocaching4Locus|LocusTesting";
	
	private static final Version LOCUS_MIN_VERSION = Version.parseVersion("1.14.6.6");
	
	public static boolean isLocusInstalled(Context context) {
		Version locusVersion = Version.parseVersion(LocusUtils.getLocusVersion(context));
		
		Log.i(TAG, "Locus version: " + locusVersion + "; Required version: " + LOCUS_MIN_VERSION);
		
		return locusVersion.compareTo(LOCUS_MIN_VERSION) >= 0;
	}
	
	public static void showLocusMissingError(final Activity activity) {
		showError(activity, LocusUtils.isLocusAvailable(activity) ? R.string.error_locus_old : R.string.error_locus_not_found, LOCUS_MIN_VERSION.toString(), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Uri localUri = Uri.parse("https://market.android.com/details?id=" + LocusUtils.getLocusDefaultPackageName(activity));
				Intent localIntent = new Intent("android.intent.action.VIEW", localUri);
				activity.startActivity(localIntent);
				activity.finish();
			}
		});
	}
	
	protected static void showError(Activity activity, int errorResId, String additionalMessage, DialogInterface.OnClickListener onClickListener) {
		if (activity.isFinishing())
			return;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		String message = String.format(activity.getString(errorResId), additionalMessage);
		
		builder.setMessage(Html.fromHtml(message));
		builder.setTitle(R.string.error_title);
		builder.setPositiveButton(R.string.ok_button, onClickListener);
		builder.setCancelable(false);
		builder.show();
	}
}
