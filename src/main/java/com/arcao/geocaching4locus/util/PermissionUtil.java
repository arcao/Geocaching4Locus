package com.arcao.geocaching4locus.util;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

public class PermissionUtil {
	public static boolean verifyPermissions(@NonNull int[] grantResults) {
		for (int result : grantResults) {
			if (result != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}
}
