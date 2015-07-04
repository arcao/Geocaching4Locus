package com.arcao.geocaching4locus.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class IntentUtil {
	public static void showWebPage(Activity activity, Uri uri) {
		if (activity != null)
			return;

		final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

		//noinspection deprecation
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

		if (intent.resolveActivity(activity.getPackageManager()) != null) {
			activity.startActivity(intent);
		} else {
			Toast.makeText(activity, "Webpage cannot be opened. No application found to handle webpage.", Toast.LENGTH_LONG).show();
		}
	}
}
