package com.arcao.geocaching4locus.importgc

import android.content.Context
import android.net.Uri
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetGeocacheCodeFromGuidUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.AnalyticsUtil
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.FilterPreferences
import com.arcao.geocaching4locus.base.util.hasExternalStoragePermission
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.base.util.isLocusNotInstalled
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import locus.api.android.ActionDisplayPointsExtended
import timber.log.Timber
import java.util.regex.Pattern

class ImportUrlViewModel(
    private val context: Context,
    private val accountManager: AccountManager,
    private val exceptionHandler: ExceptionHandler,
    private val getGeocacheCodeFromGuidUseCase: GetGeocacheCodeFromGuidUseCase,
    private val getPointFromGeocacheCodeUseCase: GetPointFromGeocacheCodeUseCase,
    private val writePointToPackPointsFileUseCase: WritePointToPackPointsFileUseCase,
    private val filterPreferences: FilterPreferences,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<ImportUrlAction>()

    fun startImport(uri: Uri) = mainLaunch {
        if (context.isLocusNotInstalled()) {
            action(ImportUrlAction.LocusMapNotInstalled)
            return@mainLaunch
        }

        if (accountManager.account == null) {
            action(ImportUrlAction.SignIn)
            return@mainLaunch
        }

        if (!context.hasExternalStoragePermission) {
            action(ImportUrlAction.RequestExternalStoragePermission)
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

                val point = getPointFromGeocacheCodeUseCase(
                    geocacheCode = geocacheCode,
                    liteData = !accountManager.isPremium,
                    geocacheLogsCount = filterPreferences.geocacheLogsCount
                )
                writePointToPackPointsFileUseCase(point)

                mainContext {
                    val intent = ActionDisplayPointsExtended.createSendPacksIntent(
                        ActionDisplayPointsExtended.cacheFileName,
                        true,
                        true
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

        return@computationContext getGeocacheCodeFromGuidUseCase(guid)
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