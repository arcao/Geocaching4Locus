package com.arcao.geocaching4locus.base.util

import android.app.Activity
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Point

object LocusMapUtil {
    @JvmStatic
    @Deprecated("Use context.getLocusVersion()", ReplaceWith("context.getLocusVersion()"))
    fun getLocusVersion(context: Context): LocusUtils.LocusVersion = context.getLocusVersion()

    @JvmStatic
    @Deprecated("Use context.isLocusNotInstalled()", ReplaceWith("context.isLocusNotInstalled()"))
    fun isLocusNotInstalled(context: Context) = context.isLocusNotInstalled()

    @JvmStatic
    @Deprecated("Use FragmentActivity.showLocusMissingError()", ReplaceWith("activity.showLocusMissingError()"))
    fun showLocusMissingError(activity: FragmentActivity) = activity.showLocusMissingError()

    @JvmStatic
    @Deprecated("Use Point.isGeocache()", ReplaceWith("point.isGeocache()"))
    fun isGeocache(point: Point?) = point.isGeocache()
}

@Suppress("NOTHING_TO_INLINE")
@Throws(LocusMapRuntimeException::class)
inline fun Context.getLocusVersion() = try {
    LocusUtils.getActiveVersion(this) ?: throw IllegalStateException("Locus is not installed.")
} catch (t: Throwable) {
    throw LocusMapRuntimeException(t)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Context.isLocusNotInstalled(): Boolean {
    val lv = LocusUtils.getActiveVersion(this)
    return lv == null || !lv.isVersionValid(AppConstants.LOCUS_MIN_VERSION_CODE)
}

@Suppress("NOTHING_TO_INLINE")
inline fun FragmentActivity.showLocusMissingError() = LocusTestingErrorDialogFragment.newInstance(this)
    .show(supportFragmentManager, LocusTestingErrorDialogFragment.FRAGMENT_TAG)

@Suppress("NOTHING_TO_INLINE")
inline fun Point?.isGeocache() = this?.gcData?.cacheID?.startsWith("GC", true) ?: false

@Suppress("NOTHING_TO_INLINE")
inline fun Activity.isCalledFromLocusMap(): Boolean {
    return IntentHelper.isIntentMainFunction(intent) ||
        IntentHelper.isIntentMainFunctionGc(intent) ||
        intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)
}