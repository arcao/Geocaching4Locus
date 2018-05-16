package com.arcao.feedback.collector;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import timber.log.Timber;

public class AppInfoCollector extends Collector {
    private final Context context;

    public AppInfoCollector(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String getName() {
        return "APP INFO";
    }

    @Override
    protected String collect() {
        final StringBuilder sb = new StringBuilder();

        final PackageInfo pi = getPackageInfo();
        if (pi != null) {
            sb.append("APP_PACKAGE=").append(pi.packageName).append("\n");
            sb.append("APP_VERSION_CODE=").append(pi.versionCode).append("\n");
            sb.append("APP_VERSION_NAME=").append(pi.versionName).append("\n");
        }

        return sb.toString();
    }

    private PackageInfo getPackageInfo() {
        final PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }

        try {
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.v("Failed to find PackageInfo for current App : %s", context.getPackageName());
            return null;
        } catch (RuntimeException e) {
            // To catch RuntimeException("Package manager has died") that can occur on some version of Android,
            // when the remote PackageManager is unavailable. I suspect this sometimes occurs when the App is being reinstalled.
            return null;
        }
    }
}
