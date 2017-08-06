package com.arcao.geocaching4locus.base.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import com.arcao.geocaching4locus.error.fragment.ExternalStoragePermissionWarningDialogFragment;

public class PermissionUtil {
	private static final int REQUEST_PERMISSION_BASE = 100;
	public static final int REQUEST_LOCATION_PERMISSION = REQUEST_PERMISSION_BASE + 1;
	public static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = REQUEST_PERMISSION_BASE + 2;

	public static final String[] PERMISSION_LOCATION_GPS =
			{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
	public static final String[] PERMISSION_LOCATION_WIFI =
			{ Manifest.permission.ACCESS_COARSE_LOCATION};
	public static final String[] PERMISSION_EXTERNAL_STORAGE =
			{ Manifest.permission.WRITE_EXTERNAL_STORAGE};

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

	public static boolean requestExternalStoragePermission(@NonNull Activity activity) {
		if (PermissionUtil.hasPermission(activity, PERMISSION_EXTERNAL_STORAGE)) {
			return true;
		} else {
			ExternalStoragePermissionWarningDialogFragment.newInstance().show(activity.getFragmentManager(), ExternalStoragePermissionWarningDialogFragment.FRAGMENT_TAG);
			return false;
		}
	}
}
