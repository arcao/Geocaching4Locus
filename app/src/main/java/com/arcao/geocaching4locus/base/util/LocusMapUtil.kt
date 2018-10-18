package com.arcao.geocaching4locus.base.util

import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Waypoint
import timber.log.Timber

object LocusMapUtil {
    @JvmStatic
    fun getLocusVersion(context: Context): LocusUtils.LocusVersion {
        try {

            return LocusUtils.getActiveVersion(context)
                    ?: throw IllegalStateException("Locus is not installed.")
        } catch (t: Throwable) {
            throw LocusMapRuntimeException(t)
        }

    }

    @JvmStatic
    fun isLocusNotInstalled(context: Context): Boolean {
        val lv = LocusUtils.getActiveVersion(context)

        val locusVersion = lv?.versionName ?: ""

        Timber.v("Locus version: $locusVersion; Required version: ${AppConstants.LOCUS_MIN_VERSION}")

        return lv == null || !lv.isVersionValid(AppConstants.LOCUS_MIN_VERSION_CODE)
    }

    @JvmStatic
    fun showLocusMissingError(activity: FragmentActivity) {
        LocusTestingErrorDialogFragment.newInstance(activity).show(activity.supportFragmentManager, LocusTestingErrorDialogFragment.FRAGMENT_TAG)
    }

    @JvmStatic
    fun isGeocache(wpt: Waypoint?): Boolean {
        return wpt?.gcData?.cacheID?.startsWith("GC", true) ?: false
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Context.getLocusVersion() = LocusMapUtil.getLocusVersion(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Context.isLocusNotInstalled() = LocusMapUtil.isLocusNotInstalled(this)

@Suppress("NOTHING_TO_INLINE")
inline fun FragmentActivity.showLocusMissingError() = LocusMapUtil.showLocusMissingError(this)

@Suppress("NOTHING_TO_INLINE")
inline fun Waypoint?.isGeocache() = LocusMapUtil.isGeocache(this)
