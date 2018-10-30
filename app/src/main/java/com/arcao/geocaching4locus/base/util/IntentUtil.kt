package com.arcao.geocaching4locus.base.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.widget.toast

object IntentUtil {
    @JvmStatic
    @Deprecated("Use activity.showWebPage(uri) instead", ReplaceWith("activity.showWebPage(uri)"))
    fun showWebPage(activity: Activity, uri: Uri): Boolean {
        return activity.showWebPage(uri)
    }

    @JvmStatic
    @Deprecated("Use intent.isCallableWith(context) instead.", ReplaceWith("intent.isCallableWith(context)"))
    fun isIntentCallable(context: Context, intent: Intent): Boolean {
        return intent.isCallableWith(context)
    }
}

fun Activity.showWebPage(uri: Uri): Boolean {
    @Suppress("DEPRECATION")
    val intent = Intent(Intent.ACTION_VIEW, uri)
            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

    return if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
        true
    } else {
        toast("Web page cannot be opened. No application found to show web pages.", Toast.LENGTH_LONG)
        false
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Intent.isCallableWith(context: Context): Boolean {
    return context.packageManager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()
}