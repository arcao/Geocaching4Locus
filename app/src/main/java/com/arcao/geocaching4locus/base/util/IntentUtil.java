package com.arcao.geocaching4locus.base.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

public final class IntentUtil {
    private IntentUtil() {
    }

    public static boolean showWebPage(Activity activity, Uri uri) {
        if (activity == null)
            return false;

        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
            return true;
        } else {
            Toast.makeText(activity, "Web page cannot be opened. No application found to show web pages.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static boolean isIntentCallable(Context context, Intent intent) {
        return !context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isEmpty();
    }

}
