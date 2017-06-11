package com.arcao.geocaching4locus.import_gc.data;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.res.ResourcesCompat;

import com.arcao.geocaching4locus.import_gc.model.ShortcutModel;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class ShortcutResolver {
    private final Context context;
    private final PackageManager packageManager;

    public ShortcutResolver(Context context) {
        this.context = context.getApplicationContext();
        packageManager = context.getPackageManager();
    }

    public List<ShortcutModel> resolve(Intent forIntent) {
        List<ResolveInfo> infoList = packageManager.queryIntentActivities(forIntent, -1);

        List<ShortcutModel> list = new ArrayList<>(infoList.size());

        for (ResolveInfo info: infoList) {
            list.add(createModel(info, forIntent));
        }

        return list;
    }

    private ShortcutModel createModel(ResolveInfo info, Intent intent) {
        ShortcutModel.Builder builder = ShortcutModel.builder();

        int density = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getLauncherLargeIconDensity();

        CharSequence title = info.loadLabel(packageManager);
        if (title == null) title = "";
        builder.title(title);

        Drawable drawable = null;
        try {
            if (info.resolvePackageName != null && info.icon != 0) {
                drawable = loadIcon(packageManager.getResourcesForApplication(info.resolvePackageName), info.icon, density);
            }
            if (drawable == null) {
                int iconResource = info.getIconResource();
                if (info.resolvePackageName != null && iconResource != 0) {
                    drawable = loadIcon(packageManager.getResourcesForApplication(info.resolvePackageName), iconResource, density);
                }
            }
        } catch (Throwable e) {
            Timber.e("Can't get resources for package=" + info.resolvePackageName, e);
        }

        if (drawable == null) {
            drawable = info.loadIcon(packageManager);
        }
        builder.icon(drawable);
        builder.intent(new Intent(intent).setClassName(info.activityInfo.packageName, info.activityInfo.name));

        return builder.build();
    }

    private static Drawable loadIcon(Resources resources, @DrawableRes int res, int density) {
        try {
            return ResourcesCompat.getDrawableForDensity(resources, res, density, null);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }
}
