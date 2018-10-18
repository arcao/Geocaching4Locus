package com.arcao.geocaching4locus.base.util.backup

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper
import android.content.Context

class PreferencesBackupAgent : BackupAgentHelper() {
    override fun onCreate() {
        val helper = SharedPreferencesBackupHelper(this, getDefaultSharedPreferencesName(this))
        addHelper(PREFS_BACKUP_KEY, helper)
    }

    companion object {
        // A key to uniquely identify the set of backup data
        private const val PREFS_BACKUP_KEY = "PREFERENCES"

        private fun getDefaultSharedPreferencesName(context: Context): String =
                "${context.packageName}_preferences"
    }
}