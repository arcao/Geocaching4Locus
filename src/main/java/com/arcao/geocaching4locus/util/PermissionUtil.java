package com.arcao.geocaching4locus.util;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

public class PermissionUtil {
	public static final String[] PERMISSION_LOCATION_GPS = new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
	public static final String[] PERMISSION_LOCATION_WIFI = new String[] { Manifest.permission.ACCESS_COARSE_LOCATION};

	public static boolean verifyPermissions(@NonNull int[] grantResults) {
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
}
