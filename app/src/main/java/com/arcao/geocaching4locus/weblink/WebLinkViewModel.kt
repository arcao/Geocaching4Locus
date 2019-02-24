package com.arcao.geocaching4locus.weblink

import android.net.Uri
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import locus.api.objects.extra.Point

abstract class WebLinkViewModel(
    private val accountManager: AccountManager,
    private val getPointFromGeocacheCode: GetPointFromGeocacheCodeUseCase,
    private val exceptionHandler: ExceptionHandler,
    dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {
    val action: Command<WebLinkAction> = Command()

    protected open val isPremiumMemberRequired: Boolean
        get() = false

    protected abstract fun getWebLink(point: Point): Uri?

    protected open fun isRefreshRequired(point: Point): Boolean {
        return false
    }

    fun resolveUri(point: Point) = mainLaunch {
        if (point.gcData == null || point.gcData.cacheID.isNullOrEmpty()) {
            action(WebLinkAction.Cancel)
            return@mainLaunch
        }

        if (isPremiumMemberRequired && !accountManager.isPremium) {
            action(WebLinkAction.PremiumMembershipRequired)
            return@mainLaunch
        }

        try {
            val uri = if (!isRefreshRequired(point)) {
                getWebLink(point)
            } else {
                if (accountManager.account == null) {
                    action(WebLinkAction.SignIn)
                    return@mainLaunch
                }

                val newPoint = showProgress(R.string.progress_download_geocache) {
                    getPointFromGeocacheCode(point.gcData.cacheID)
                }
                getWebLink(newPoint)
            }

            if (uri == null) {
                action(WebLinkAction.Cancel)
                return@mainLaunch
            }

            action(WebLinkAction.ShowUri(uri))
        } catch (e: Throwable) {
            action(WebLinkAction.Error(exceptionHandler(e)))
        }
    }

    fun cancelRetrieveUri() {
        job.cancel()
        action(WebLinkAction.Cancel)
    }
}
