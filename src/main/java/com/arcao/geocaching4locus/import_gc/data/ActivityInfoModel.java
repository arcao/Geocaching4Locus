package com.arcao.geocaching4locus.import_gc.data;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;

import timber.log.Timber;

public class ActivityInfoModel {
    public final Drawable icon;
    public final CharSequence title;
    public final Intent intent;

    public ActivityInfoModel(Context context, ResolveInfo info, Intent intent) {
        PackageManager pm = context.getPackageManager();
        int density = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getLauncherLargeIconDensity();

        CharSequence title = info.loadLabel(pm);
        if (title == null) title = "";
        this.title = title;

        Drawable drawable = null;
        try {
            if (info.resolvePackageName != null && info.icon != 0) {
                drawable = loadIcon(pm.getResourcesForApplication(info.resolvePackageName), info.icon, density);
            }
            if (drawable == null) {
                int iconResource = info.getIconResource();
                if (info.resolvePackageName != null && iconResource != 0) {
                    drawable = loadIcon(pm.getResourcesForApplication(info.resolvePackageName), iconResource, density);
                }
            }
        } catch (Throwable e) {
            Timber.e("Can't get resources for package=" + info.resolvePackageName, e);
        }

        if (drawable == null) {
            drawable = info.loadIcon(pm);
        }
        this.icon = drawable;

        this.intent = new Intent(intent).setClassName(info.activityInfo.packageName, info.activityInfo.name);
    }

    private static Drawable loadIcon(Resources resources, @DrawableRes int res, int density) {
        try {
            return ResourcesCompat.getDrawableForDensity(resources, res, density, null);
        } catch (NotFoundException e) {
            return null;
        }
    }
}
