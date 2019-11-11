package com.arcao.geocaching4locus.base.util

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import com.arcao.geocaching4locus.error.fragment.LocusTestingErrorDialogFragment
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusConst
import locus.api.objects.extra.Point
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("NOTHING_TO_INLINE")
inline fun FragmentActivity.showLocusMissingError() =
    LocusTestingErrorDialogFragment.newInstance(this).show(supportFragmentManager)

@UseExperimental(ExperimentalContracts::class)
@Suppress("NOTHING_TO_INLINE")
inline fun Point?.isGeocache(): Boolean {
    contract {
        returns(true) implies (this@isGeocache != null)
    }

    return this?.gcData?.cacheID?.startsWith("GC", true) ?: false
}

@Suppress("NOTHING_TO_INLINE")
inline fun Activity.isCalledFromLocusMap(): Boolean {
    return IntentHelper.isIntentMainFunction(intent) ||
        IntentHelper.isIntentMainFunctionGc(intent) ||
        intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)
}
