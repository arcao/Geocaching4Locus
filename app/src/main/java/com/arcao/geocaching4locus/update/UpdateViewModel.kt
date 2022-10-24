package com.arcao.geocaching4locus.update

import android.app.Application
import android.content.Intent
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetGeocachingLogsUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.util.AnalyticsManager
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusUtils
import locus.api.manager.LocusMapManager
import locus.api.mapper.PointMerger
import locus.api.mapper.Util
import locus.api.objects.geoData.Point
import timber.log.Timber

class UpdateViewModel(
    private val context: Application,
    private val accountManager: AccountManager,
    private val defaultPreferenceManager: DefaultPreferenceManager,
    private val getPointFromGeocacheCode: GetPointFromGeocacheCodeUseCase,
    private val getGeocachingLogs: GetGeocachingLogsUseCase,
    private val pointMerger: PointMerger,
    private val locusMapManager: LocusMapManager,
    private val exceptionHandler: ExceptionHandler,
    private val analyticsManager: AnalyticsManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<UpdateAction>()
    private var job: Job? = null

    fun processIntent(intent: Intent) {
        if (locusMapManager.isLocusMapNotInstalled) {
            action(UpdateAction.LocusMapNotInstalled)
            return
        }

        if (accountManager.account == null) {
            action(UpdateAction.SignIn)
            return
        }

        if (!accountManager.isPremium && isUpdateLogsIntent(intent)) {
            action(UpdateAction.PremiumMembershipRequired)
            return
        }

        if (job?.isActive == true) {
            job?.cancel()
        }

        job = mainLaunch {
            val downloadFullGeocacheOnShow = defaultPreferenceManager.downloadFullGeocacheOnShow

            try {
                showProgress(R.string.progress_update_geocache, maxProgress = 1) {

                    val updateData = computationContext {
                        val updateData =
                            retrieveUpdateData(intent) ?: return@computationContext null

                        val basicMember = !(accountManager.isPremium)
                        var logsCount = defaultPreferenceManager.downloadingGeocacheLogsCount
                        var lite = false

                        if (basicMember) {
                            logsCount = 0
                            lite = true
                        }

                        updateData.newPoint =
                            getPointFromGeocacheCode(updateData.geocacheCode, lite, logsCount)

                        if (updateData.downloadLogs) {
                            var progress = updateData.newPoint.gcData?.logs?.count() ?: 0

                            logsCount = AppConstants.LOGS_TO_UPDATE_MAX

                            updateProgress(
                                R.string.progress_download_logs,
                                progress = progress,
                                maxProgress = logsCount
                            )

                            val logs = getGeocachingLogs(
                                updateData.geocacheCode,
                                progress,
                                logsCount
                            ).map {
                                progress += it.count()
                                updateProgress(progress = progress, maxProgress = logsCount)
                                it
                            }.toList()

                            updateData.newPoint.gcData?.logs?.apply {
                                addAll(logs.flatten())
                                sortBy {
                                    it.date
                                }
                            }
                        }

                        if (updateData.downloadLogs && updateData.oldPoint != null && !defaultPreferenceManager.downloadLogsUpdateCache) {
                            pointMerger.mergeGeocachingLogs(
                                updateData.oldPoint,
                                updateData.newPoint
                            )

                            // only when this feature is enabled
                            if (defaultPreferenceManager.disableDnfNmNaGeocaches)
                                Util.applyUnavailabilityForGeocache(
                                    updateData.oldPoint,
                                    defaultPreferenceManager.disableDnfNmNaGeocachesThreshold
                                )

                            updateData.newPoint = updateData.oldPoint
                        } else {
                            pointMerger.mergePoints(updateData.newPoint, updateData.oldPoint)

                            if (downloadFullGeocacheOnShow) {
                                updateData.newPoint.removeExtraOnDisplay()
                            }
                        }

                        return@computationContext updateData
                    }

                    if (updateData == null) {
                        action(UpdateAction.Cancel)
                        return@showProgress
                    }

                    // if Point is already in DB we must update it manually
                    if (updateData.oldPoint != null) {
                        locusMapManager.updatePoint(updateData.newPoint)
                        action(UpdateAction.Finish())
                    } else {
                        action(
                            UpdateAction.Finish(
                                LocusUtils.prepareResultExtraOnDisplayIntent(
                                    updateData.newPoint,
                                    downloadFullGeocacheOnShow
                                )
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                action(UpdateAction.Error(exceptionHandler(e)))
            }
        }
    }

    private fun retrieveUpdateData(intent: Intent): UpdateData? {
        var cacheId: String? = null
        var oldPoint: Point? = null

        when {
            intent.hasExtra(UpdateActivity.PARAM_CACHE_ID) ->
                cacheId = intent.getStringExtra(UpdateActivity.PARAM_CACHE_ID)

            IntentHelper.isIntentPointTools(intent) -> try {
                val p = IntentHelper.getPointFromIntent(context, intent)

                if (p?.gcData != null) {
                    cacheId = p.gcData?.cacheID
                    oldPoint = p
                }
            } catch (t: Throwable) {
                Timber.e(t)
            }

            intent.hasExtra(UpdateActivity.PARAM_SIMPLE_CACHE_ID) -> {
                cacheId = intent.getStringExtra(UpdateActivity.PARAM_SIMPLE_CACHE_ID)

                if (!defaultPreferenceManager.downloadGeocacheOnShow) {
                    Timber.d("Updating simple cache on displaying is not allowed!")
                    cacheId = null
                }
            }
        }

        if (cacheId == null || !cacheId.uppercase().startsWith("GC")) {
            Timber.e("cacheId/simpleCacheId not found")
            return null
        }

        val downloadLogs =
            AppConstants.UPDATE_WITH_LOGS_COMPONENT == intent.component?.className

        analyticsManager.actionUpdate(oldPoint != null, downloadLogs, accountManager.isPremium)
        return UpdateData(cacheId, downloadLogs, oldPoint)
    }

    private fun isUpdateLogsIntent(intent: Intent) =
        AppConstants.UPDATE_WITH_LOGS_COMPONENT == intent.component?.className

    fun cancelProgress() {
        job?.cancel()
    }

    class UpdateData(
        val geocacheCode: String,
        val downloadLogs: Boolean,
        val oldPoint: Point? = null
    ) {
        lateinit var newPoint: Point
    }
}
