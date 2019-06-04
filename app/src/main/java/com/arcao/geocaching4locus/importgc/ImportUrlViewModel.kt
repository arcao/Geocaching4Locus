package com.arcao.geocaching4locus.importgc

import android.net.Uri
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.isPremium
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetGeocacheCodeFromGuidUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.AnalyticsUtil
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.settings.manager.FilterPreferenceManager
import locus.api.manager.LocusMapManager
import timber.log.Timber
import java.util.regex.Pattern

class ImportUrlViewModel(
    private val accountManager: AccountManager,
    private val exceptionHandler: ExceptionHandler,
    private val getGeocacheCodeFromGuid: GetGeocacheCodeFromGuidUseCase,
    private val getPointFromGeocacheCode: GetPointFromGeocacheCodeUseCase,
    private val writePointToPackPointsFile: WritePointToPackPointsFileUseCase,
    private val filterPreferenceManager: FilterPreferenceManager,
    private val locusMapManager: LocusMapManager,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<ImportUrlAction>()

    fun startImport(uri: Uri) = mainLaunch {
        if (locusMapManager.isLocusMapNotInstalled) {
            action(ImportUrlAction.LocusMapNotInstalled)
            return@mainLaunch
        }

        if (accountManager.account == null) {
            action(ImportUrlAction.SignIn)
            return@mainLaunch
        }

        performImport(uri)
    }

    private suspend fun performImport(uri: Uri) = computationContext {
        AnalyticsUtil.actionImport(accountManager.isPremium)

        try {
            showProgress(R.string.progress_download_geocache) {
                val geocacheCode = retrieveGeocacheCode(uri)
                if (geocacheCode == null) {
                    mainContext {
                        action(ImportUrlAction.Cancel)
                    }
                    return@showProgress
                }

                Timber.i("source: import;%s", geocacheCode)

                val point = getPointFromGeocacheCode(
                    referenceCode = geocacheCode,
                    liteData = !accountManager.isPremium,
                    geocacheLogsCount = filterPreferenceManager.geocacheLogsCount
                )
                writePointToPackPointsFile(point)

                mainContext {
                    val intent = locusMapManager.createSendPointsIntent(
                        callImport = true,
                        center = true
                    )
                    action(ImportUrlAction.Finish(intent))
                }
            }
        } catch (e: Exception) {
            mainContext {
                action(ImportUrlAction.Error(exceptionHandler(e)))
            }
        }
    }

    private suspend fun retrieveGeocacheCode(uri: Uri): String? = computationContext {
        val url = uri.toString()

        val cacheCodeMatcher = CACHE_CODE_PATTERN.matcher(url)
        if (cacheCodeMatcher.find()) {
            return@computationContext cacheCodeMatcher.group(1)
        }

        val guidMatcher = GUID_PATTERN.matcher(url)
        if (!guidMatcher.find()) {
            return@computationContext null
        }

        val guid = guidMatcher.group(1)

        return@computationContext getGeocacheCodeFromGuid(guid)
    }

    fun cancelImport() {
        job.cancel()
        action(ImportUrlAction.Cancel)
    }

    companion object {
        val CACHE_CODE_PATTERN: Pattern = Pattern.compile("(GC[A-HJKMNPQRTV-Z0-9]+)", Pattern.CASE_INSENSITIVE)
        private val GUID_PATTERN = Pattern.compile(
            "guid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})",
            Pattern.CASE_INSENSITIVE
        )
    }
}