package com.arcao.geocaching4locus.weblink

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.arcao.geocaching4locus.authentication.util.AccountManager
import com.arcao.geocaching4locus.base.BaseViewModel
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.util.Command
import com.arcao.geocaching4locus.base.util.invoke
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import com.arcao.geocaching4locus.weblink.usecase.GetPointFromGeocacheCodeUseCase
import kotlinx.coroutines.launch
import locus.api.objects.extra.Point

abstract class WebLinkViewModel(
        private val accountManager: AccountManager,
        private val getPointFromGeocacheCodeUseCase: GetPointFromGeocacheCodeUseCase,
        private val exceptionHandler: ExceptionHandler,
        dispatcherProvider: CoroutinesDispatcherProvider
) : BaseViewModel(dispatcherProvider) {

    val progressVisible: MutableLiveData<Boolean> = MutableLiveData()
    val action: Command<WebLinkAction> = Command()

    protected open val isPremiumMemberRequired: Boolean
        get() = false

    protected abstract fun getWebLink(point: Point): Uri?

    protected open fun isRefreshRequired(point: Point): Boolean {
        return false
    }

    fun retrieveUri(point: Point) = launch {
        if (point.gcData == null || point.gcData.cacheID.isNullOrEmpty()) {
            mainContext {
                action(WebLinkAction.NavigationBack)
            }
            return@launch
        }

        if (accountManager.account == null) {
            mainContext {
                action(WebLinkAction.SignIn)
            }
            return@launch
        }

        if (isPremiumMemberRequired && !accountManager.isPremium) {
            mainContext {
                action(WebLinkAction.PremiumMembershipRequired)
            }
            return@launch
        }

        try {
            val uri = if (!isRefreshRequired(point)) {
                getWebLink(point)
            } else {
                progressVisible(true)
                val newPoint = getPointFromGeocacheCodeUseCase(point.gcData.cacheID)
                progressVisible(false)
                getWebLink(newPoint)
            }

            if (uri == null) {
                mainContext {
                    action(WebLinkAction.NavigationBack)
                }
                return@launch
            }

            mainContext {
                action(WebLinkAction.ResolvedUri(uri))
            }
        } catch (e: Throwable) {
            mainContext {
                action(WebLinkAction.Error(exceptionHandler.handle(e)))
            }
        }
    }

    fun cancelRetrieveUri() {
        job.cancel()
    }
}

