package com.arcao.geocaching4locus.update

import android.content.Intent
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetOldPointNewPointPairFromPointUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointsFromPointIndexesUseCase
import com.arcao.geocaching4locus.base.util.AnalyticsUtil
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import locus.api.android.utils.IntentHelper
import locus.api.manager.LocusMapManager
import locus.api.mapper.PointMerger
import timber.log.Timber

class UpdateMoreViewModel(
    private val accountManager: AccountManager,
    private val defaultPreferenceManager: DefaultPreferenceManager,
    private val getPointsFromPointIndexes: GetPointsFromPointIndexesUseCase,
    private val getOldPointNewPointPairFromPoint: GetOldPointNewPointPairFromPointUseCase,
    private val locusMapManager: LocusMapManager,
    private val merger: PointMerger,
    private val exceptionHandler: ExceptionHandler,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<UpdateMoreAction>()
    private var job: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun processIntent(intent: Intent) {
        if (locusMapManager.isLocusMapNotInstalled) {
            action(UpdateMoreAction.LocusMapNotInstalled)
            return
        }

        if (accountManager.account == null) {
            action(UpdateMoreAction.SignIn)
            return
        }

        if (job?.isActive == true) {
            job?.cancel()
        }

        job = mainLaunch {
            try {
                var progress = 0
                showProgress(R.string.progress_update_geocaches, maxProgress = 1) {
                    computationContext {
                        var pointIndexes: LongArray? = null

                        if (IntentHelper.isIntentPointsTools(intent))
                            pointIndexes = IntentHelper.getPointsFromIntent(intent)

                        AnalyticsUtil.actionUpdateMore(
                            pointIndexes?.size ?: 0,
                            accountManager.isPremium
                        )
                        Timber.i("source: update;count=%d", pointIndexes?.size ?: 0)

                        if (pointIndexes?.isNotEmpty() != true) {
                            action(UpdateMoreAction.Cancel)
                            return@computationContext
                        }

                        val basicMember = !(accountManager.isPremium)
                        var logsCount = defaultPreferenceManager.downloadingGeocacheLogsCount
                        var lite = false

                        if (basicMember) {
                            logsCount = 0
                            lite = true
                        }

                        val existingPoints = getPointsFromPointIndexes(this, pointIndexes)

                        val pointPairs =
                            getOldPointNewPointPairFromPoint(this, existingPoints, lite, logsCount)
                        for ((oldPoint, newPoint) in pointPairs) {
                            if (newPoint == null) continue

                            merger.mergePoints(newPoint, oldPoint)
                            locusMapManager.updatePoint(newPoint)
                            progress++
                            updateProgress(progress = progress, maxProgress = pointIndexes.size)
                        }
                    }
                }

                action(UpdateMoreAction.Finish)
            } catch (e: Exception) {
                action(UpdateMoreAction.Error(exceptionHandler(e)))
            }
        }
    }

    fun cancelProgress() {
        job?.cancel()
    }
}
