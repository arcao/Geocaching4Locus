package com.arcao.geocaching4locus.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import timber.log.Timber;

public class PreferencesBackupAgent extends BackupAgentHelper {
	// A key to uniquely identify the set of backup data
	private static final String PREFS_BACKUP_KEY = "PREFERENCES";

	@Override
	public void onCreate() {
		BackupHelper helper = new SharedPreferencesBackupHelper(this, getDefaultSharedPreferencesName(this));
		addHelper(PREFS_BACKUP_KEY, helper);
	}

	private static String getDefaultSharedPreferencesName(Context context) {
		String name = context.getPackageName() + "_preferences";
		Timber.i("getDefaultSharedPreferencesName: " + name);
		return name;
	}
}