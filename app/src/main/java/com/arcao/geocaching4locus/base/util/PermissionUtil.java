package com.arcao.geocaching4locus.base.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.arcao.geocaching4locus.error.fragment.ExternalStoragePermissionWarningDialogFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public final class PermissionUtil {
    private static final int REQUEST_PERMISSION_BASE = 100;
    public static final int REQUEST_LOCATION_PERMISSION = REQUEST_PERMISSION_BASE + 1;
    public static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = REQUEST_PERMISSION_BASE + 2;

    public static final String[] PERMISSION_LOCATION_GPS =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] PERMISSION_LOCATION_WIFI =
            {Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] PERMISSION_EXTERNAL_STORAGE =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private PermissionUtil() {
    }

    public static boolean verifyPermissions(@NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasPermission(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED)
                return false;
        }

        return true;
    }

    public static boolean requestExternalStoragePermission(@NonNull AppCompatActivity activity) {
        if (PermissionUtil.hasPermission(activity, PERMISSION_EXTERNAL_STORAGE)) {
            return true;
        } else {
            ExternalStoragePermissionWarningDialogFragment.newInstance().show(activity.getSupportFragmentManager(), ExternalStoragePermissionWarningDialogFragment.FRAGMENT_TAG);
            return false;
        }
    }
}
