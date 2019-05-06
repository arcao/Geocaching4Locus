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

@Suppress("NOTHING_TO_INLINE")
inline fun Context.isLocusNotInstalled(): Boolean {
    val lv = LocusUtils.getActiveVersion(this)
    return lv == null || !lv.isVersionValid(AppConstants.LOCUS_MIN_VERSION_CODE)
}

@Suppress("NOTHING_TO_INLINE")
inline fun FragmentActivity.showLocusMissingError() =
    LocusTestingErrorDialogFragment.newInstance(this).show(supportFragmentManager)

@Suppress("NOTHING_TO_INLINE")
inline fun Point?.isGeocache() = this?.gcData?.cacheID?.startsWith("GC", true) ?: false

@Suppress("NOTHING_TO_INLINE")
inline fun Activity.isCalledFromLocusMap(): Boolean {
    return IntentHelper.isIntentMainFunction(intent) ||
        IntentHelper.isIntentMainFunctionGc(intent) ||
        intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)
}