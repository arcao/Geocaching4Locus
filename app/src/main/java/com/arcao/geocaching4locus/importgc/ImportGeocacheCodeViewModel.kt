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
import com.arcao.geocaching4locus.base.util.FilterPreferences
import com.arcao.geocaching4locus.base.util.hasExternalStoragePermission
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.isLocusNotInstalled
import com.arcao.geocaching4locus.error.exception.IntendedException
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.map
import kotlinx.coroutines.launch
import locus.api.android.ActionDisplayPointsExtended
import java.util.regex.Pattern

class ImportGeocacheCodeViewModel(
    private val context: Context,
    private val accountManager: AccountManager,
    private val exceptionHandler: ExceptionHandler,
    private val getPointsFromGeocacheCodesUseCase: GetPointsFromGeocacheCodesUseCase,
    private val writePointToPackPointsFileUseCase: WritePointToPackPointsFileUseCase,
    private val filterPreferences: FilterPreferences,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<ImportGeocacheCodeAction>()

    fun init() = launch {
        if (context.isLocusNotInstalled()) {
            mainContext {
                action(ImportGeocacheCodeAction.LocusMapNotInstalled)
            }
            return@launch
        }

        if (accountManager.account == null) {
            mainContext {
                action(ImportGeocacheCodeAction.SignIn)
            }
            return@launch
        }

        if (!context.hasExternalStoragePermission) {
            mainContext {
                action(ImportGeocacheCodeAction.RequestExternalStoragePermission)
            }
            return@launch
        }

        mainContext {
            action(ImportGeocacheCodeAction.GeocacheCodesInput)
        }
    }

    @ExperimentalCoroutinesApi
    fun importGeocacheCodes(geocacheCodes: Array<String>) = launch {
        val importIntent = ActionDisplayPointsExtended.createSendPacksIntent(
                ActionDisplayPointsExtended.cacheFileName,
                true,
                true
        )

        var receivedGeocaches = 0

        try {
            showProgress(R.string.progress_download_geocaches, maxProgress = geocacheCodes.size) {
                val channel = getPointsFromGeocacheCodesUseCase(
                    geocacheCodes,
                    !accountManager.isPremium,
                    filterPreferences.geocacheLogsCount
                ).map {
                    receivedGeocaches += it.size
                    updateProgress(progress = receivedGeocaches)
                    it
                }
                writePointToPackPointsFileUseCase(channel)
            }
        } catch (e: Exception) {
            mainContext {
                action(ImportGeocacheCodeAction.Error(
                        if (receivedGeocaches > 0) {
                            exceptionHandler(IntendedException(e, importIntent))
                        } else {
                            exceptionHandler(e)
                        }
                ))
            }
            return@launch
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