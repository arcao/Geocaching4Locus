package com.arcao.geocaching4locus.importgc

import android.content.Context
import com.arcao.geocaching.api.util.GeocachingUtils
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetPointsFromGeocacheCodesUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.hasExternalStoragePermission
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.isLocusNotInstalled
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import kotlinx.coroutines.channels.map
import locus.api.manager.LocusMapManager
import java.util.regex.Pattern

class ImportGeocacheCodeViewModel(
    private val context: Context,
    private val accountManager: AccountManager,
    private val exceptionHandler: ExceptionHandler,
    private val getPointsFromGeocacheCodes: GetPointsFromGeocacheCodesUseCase,
    private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
    private val filterPreferenceManager: FilterPreferenceManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<ImportGeocacheCodeAction>()

    fun init() = mainLaunch {
        if (context.isLocusNotInstalled()) {
            action(ImportGeocacheCodeAction.LocusMapNotInstalled)
            return@mainLaunch
        }

        if (accountManager.account == null) {
            action(ImportGeocacheCodeAction.SignIn)
            return@mainLaunch
        }

        if (!context.hasExternalStoragePermission) {
            action(ImportGeocacheCodeAction.RequestExternalStoragePermission)
            return@mainLaunch
        }

        action(ImportGeocacheCodeAction.GeocacheCodesInput)
    }

    fun importGeocacheCodes(geocacheCodes: Array<String>) = computationLaunch {
        val importIntent = LocusMapManager.createSendPointsIntent(
            callImport = true,
            center = true
        )

        var receivedGeocaches = 0

        try {
            showProgress(R.string.progress_download_geocaches, maxProgress = geocacheCodes.size) {
                val channel = getPointsFromGeocacheCodes(
                    this,
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

    fun parseGeocacheCodes(input: CharSequence): Array<String> {
        val geocacheCodes = PATTERN_CACHE_ID_SEPARATOR.split(input)

        for (geocacheCode in geocacheCodes) {
            if (!GeocachingUtils.isCacheCodeValid(geocacheCode)) {
                throw IllegalArgumentException("Geocache code {$geocacheCode} is not valid.")
            }
        }

        return geocacheCodes
    }

    fun cancelImport() {
        job.cancel()
        action(ImportGeocacheCodeAction.Cancel)
    }

    companion object {
        private val PATTERN_CACHE_ID_SEPARATOR = Pattern.compile("[\\W]+")
    }
}