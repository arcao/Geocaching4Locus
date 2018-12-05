package com.arcao.geocaching4locus.base.util

import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.arcao.geocaching4locus.base.constants.PrefConstants

var Context.hidePowerManagementWarning: Boolean
    get() {
        return PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PrefConstants.HIDE_POWER_MANAGEMENT_WARNING, false) ||
                !isPowerManagerPresent()
    }
    set(value) {
        PreferenceManager.getDefaultSharedPreferences(this).edit {
            putBoolean(PrefConstants.HIDE_POWER_MANAGEMENT_WARNING, value)
        }
    }

fun Context.isPowerManagerPresent(): Boolean {
    return isHuaweiPowerManagerPresent() || isXaomiPowerManagerPresent()
}

private fun Context.isHuaweiPowerManagerPresent(): Boolean {
    return Intent().setClassName(
            "com.huawei.systemmanager",
            "com.huawei.systemmanager.optimize.process.ProtectActivity"
    ).isCallableWith(this)
}

private fun Context.isXaomiPowerManagerPresent(): Boolean {
    return Intent().setClassName(
            "com.miui.powerkeeper",
            "com.miui.powerkeeper.ui.HiddenAppsContainerManagementActivity"
    ).isCallableWith(this)
}
