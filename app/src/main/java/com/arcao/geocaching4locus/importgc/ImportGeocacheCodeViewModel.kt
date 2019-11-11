package com.arcao.geocaching4locus.importgc

import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.AnalyticsUtil
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import locus.api.manager.LocusMapManager
import java.util.regex.Pattern

class ImportGeocacheCodeViewModel(
    private val accountManager: AccountManager,
    private val exceptionHandler: ExceptionHandler,
    private val getPointsFromGeocacheCodes: GetPointsFromGeocacheCodesUseCase,
    private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
    private val filterPreferenceManager: FilterPreferenceManager,
    private val locusMapManager: LocusMapManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<ImportGeocacheCodeAction>()
    private var job: Job? = null

    fun init(geocacheCodes: Array<String>?) = mainLaunch {
        if (locusMapManager.isLocusMapNotInstalled) {
            action(ImportGeocacheCodeAction.LocusMapNotInstalled)
            return@mainLaunch
        }

        if (accountManager.account == null) {
            action(ImportGeocacheCodeAction.SignIn)
            return@mainLaunch
        }

        if (geocacheCodes.isNullOrEmpty()) {
            action(ImportGeocacheCodeAction.GeocacheCodesInput)
        } else {
            importGeocacheCodes(geocacheCodes)
        }
    }

    fun importGeocacheCodes(geocacheCodes: Array<String>) {
        if (job?.isActive == true) {
            job?.cancel()
        }

        job = computationLaunch {
            AnalyticsUtil.actionImportGC(accountManager.isPremium)

            val importIntent = locusMapManager.createSendPointsIntent(
                callImport = true,
                center = true
            )

            var receivedGeocaches = 0

            try {
                showProgress(
                    R.string.progress_download_geocaches,
                    maxProgress = geocacheCodes.size
                ) {
                    val channel = getPointsFromGeocacheCodes(
                        geocacheCodes,
                        !accountManager.isPremium,
                        filterPreferenceManager.geocacheLogsCount
                    ).map {
                        receivedGeocaches += it.size
                        updateProgress(progress = receivedGeocaches)
                        it
                    }
                    writePointToPackPointsFile(channel)
                }
            } catch (e: Exception) {
                mainContext {
                    action(
                        ImportGeocacheCodeAction.Error(
                            if (receivedGeocaches > 0) {
                                exceptionHandler(IntendedException(e, importIntent))
                            } else {
                                exceptionHandler(e)
                            }
                        )
                    )
                }
                return@computationLaunch
            }

            mainContext {
                action(ImportGeocacheCodeAction.Finish(importIntent))
            }
        }
    }

    fun parseGeocacheCodes(input: CharSequence): Array<String> {
        val geocacheCodes = PATTERN_CACHE_ID_SEPARATOR.split(input)

        for (geocacheCode in geocacheCodes) {
            if (!ReferenceCode.isReferenceCodeValid(geocacheCode, ReferenceCode.GEOCACHE_PREFIX)) {
                throw IllegalArgumentException("Geocache referenceCode {$geocacheCode} is not valid.")
            }
        }

        return geocacheCodes
    }

    fun cancelImport() {
        job?.cancel()
        action(ImportGeocacheCodeAction.Cancel)
    }

    companion object {
        private val PATTERN_CACHE_ID_SEPARATOR = Pattern.compile("[\\W]+")
    }
}
