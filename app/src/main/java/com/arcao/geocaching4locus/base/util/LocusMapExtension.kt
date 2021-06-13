package com.arcao.geocaching4locus.base.util

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusConst
import locus.api.objects.geoData.Point

fun FragmentActivity.showLocusMissingError() =
    LocusTestingErrorDialogFragment.newInstance(this).show(supportFragmentManager)

fun Point.isGeocache(): Boolean = this.gcData?.cacheID?.startsWith("GC", true) ?: false

fun Activity.isCalledFromLocusMap(): Boolean {
    return IntentHelper.isIntentMainFunction(intent) ||
        IntentHelper.isIntentMainFunctionGc(intent) ||
        intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)
}
