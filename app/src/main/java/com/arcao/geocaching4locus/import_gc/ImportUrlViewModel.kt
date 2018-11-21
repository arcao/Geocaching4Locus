package com.arcao.geocaching4locus.import_gc

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetGeocacheCodeFromGuidUseCase
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.usecase.WritePointToPackPointsFileUseCase
import com.arcao.geocaching4locus.base.util.*
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import kotlinx.coroutines.launch
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
    private val preferences: SharedPreferences,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action = Command<ImportUrlAction>()

    fun startImport(uri: Uri) = launch {
        if (context.isLocusNotInstalled()) {
            mainContext {
                action(ImportUrlAction.LocusMapNotInstalled)
            }
            return@launch
        }

        if (accountManager.account == null) {
            mainContext {
                action(ImportUrlAction.SignIn)
            }
            return@launch
        }

        if (!context.hasExternalStoragePermission) {
            mainContext {
                action(ImportUrlAction.RequestExternalStoragePermission)
            }
            return@launch
        }

        performImport(uri)
    }

    private suspend fun performImport(uri: Uri) {
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

                // TODO improve
                val geocacheLogsCount = preferences.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5)

                val point = getPointFromGeocacheCodeUseCase(
                        geocacheCode = geocacheCode,
                        liteData = !accountManager.isPremium,
                        geocacheLogsCount = geocacheLogsCount
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

    private suspend fun retrieveGeocacheCode(uri: Uri): String? {
        val url = uri.toString()

        val cacheCodeMatcher = CACHE_CODE_PATTERN.matcher(url)
        if (cacheCodeMatcher.find()) {
            return cacheCodeMatcher.group(1)
        }

        val guidMatcher = GUID_PATTERN.matcher(url)
        if (!guidMatcher.find()) {
            return null
        }

        val guid = guidMatcher.group(1)

        return getGeocacheCodeFromGuidUseCase(guid)
    }

    fun cancelImport() {
        job.cancel()
        action(ImportUrlAction.Cancel)
    }

    companion object {
        val CACHE_CODE_PATTERN: Pattern = Pattern.compile("(GC[A-HJKMNPQRTV-Z0-9]+)", Pattern.CASE_INSENSITIVE)
        private val GUID_PATTERN = Pattern.compile("guid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})", Pattern.CASE_INSENSITIVE)
    }
}