package com.arcao.geocaching4locus.update

import android.content.Context
import android.content.Intent
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetOldPointNewPointPairFromPointUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromPointIndexesUseCase
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.isLocusNotInstalled
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import locus.api.android.utils.IntentHelper
import locus.api.manager.LocusMapManager
import locus.api.mapper.PointMerger
import timber.log.Timber

/**
 * Created by Arcao on 27.12.2018.
 */
@ExperimentalCoroutinesApi
class UpdateMoreViewModel(
    private val context: Context,
    private val accountManager: AccountManager,
    private val defaultPreferenceManager: DefaultPreferenceManager,
    private val getPointsFromPointIndexesUseCase: GetPointsFromPointIndexesUseCase,
    private val getOldPointNewPointPairFromPointUseCase: GetOldPointNewPointPairFromPointUseCase,
    private val locusMapManager: LocusMapManager,
    private val merger: PointMerger,
    private val exceptionHandler: ExceptionHandler,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<UpdateMoreAction>()

    fun processIntent(intent: Intent) = mainLaunch {
        if (context.isLocusNotInstalled()) {
            action(UpdateMoreAction.LocusMapNotInstalled)
            return@mainLaunch
        }

        if (accountManager.account == null) {
            action(UpdateMoreAction.SignIn)
            return@mainLaunch
        }

        try {
            var progress = 0
            showProgress(R.string.progress_update_geocaches, maxProgress = 1) {
                computationContext {
                    var pointIndexes: LongArray? = null

                    if (IntentHelper.isIntentPointsScreenTools(intent))
                        pointIndexes = IntentHelper.handleIntentPointsScreenTools(intent)

                    Timber.i("source: update;count=%d", pointIndexes?.size ?: 0)

                    if (pointIndexes?.isNotEmpty() != true) {
                        action(UpdateMoreAction.Cancel)
                        return@computationContext
                    }

                    updateProgress(maxProgress = pointIndexes.size)

                    val basicMember = !accountManager.isPremium
                    var logsCount = defaultPreferenceManager.downloadingGeocacheLogsCount
                    var lite = false

                    if (basicMember) {
                        logsCount = 0
                        lite = true
                    }

                    val existingPoints = getPointsFromPointIndexesUseCase(pointIndexes)

                    val pointPairs = getOldPointNewPointPairFromPointUseCase(existingPoints, lite, logsCount)
                    for ((oldPoint, newPoint) in pointPairs) {
                        if (newPoint == null) continue

                        merger.mergePoints(newPoint, oldPoint)
                        locusMapManager.updatePoint(newPoint)
                        progress++
                        updateProgress(progress = progress)
                    }
                }
            }
        } catch (e: Exception) {
            action(UpdateMoreAction.Error(exceptionHandler(e)))
        }
    }
}