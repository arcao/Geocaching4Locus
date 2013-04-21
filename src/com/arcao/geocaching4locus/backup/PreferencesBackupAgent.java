package com.arcao.geocaching4locus.backup;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.util.Log;

@SuppressLint("NewApi")
@TargetApi(8)
public class PreferencesBackupAgent extends BackupAgentHelper {
	private static final String TAG = "Geocaching4Locus|PreferencesBackupAgent";

	// A key to uniquely identify the set of backup data
	private static final String PREFS_BACKUP_KEY = "PREFERENCES";

	@Override
	public void onCreate() {
		SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, getDefaultSharedPreferencesName(this));
		addHelper(PREFS_BACKUP_KEY, helper);
	}

	private static String getDefaultSharedPreferencesName(Context context) {
		String name = context.getPackageName() + "_preferences";
		Log.i(TAG, "getDefaultSharedPreferencesName: " + name);
		return name;
	}
}