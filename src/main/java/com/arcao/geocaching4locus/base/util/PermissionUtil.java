package com.arcao.geocaching4locus.base.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

public class PermissionUtil {
	public static final String[] PERMISSION_LOCATION_GPS =
			{ Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
	public static final String[] PERMISSION_LOCATION_WIFI =
			{ Manifest.permission.ACCESS_COARSE_LOCATION};

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
}
