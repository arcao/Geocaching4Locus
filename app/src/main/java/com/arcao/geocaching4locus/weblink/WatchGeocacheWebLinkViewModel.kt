package com.arcao.geocaching4locus.weblink

import android.net.Uri
import com.arcao.geocaching4locus.BuildConfig
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.GetPointFromGeocacheCodeUseCase
import com.arcao.geocaching4locus.data.account.AccountManager
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import com.arcao.geocaching4locus.error.handler.ExceptionHandler
import locus.api.objects.extra.Point
import java.util.Locale

class WatchGeocacheWebLinkViewModel(
    accountManager: AccountManager,
    getPointFromGeocacheCode: GetPointFromGeocacheCodeUseCase,
    exceptionHandler: ExceptionHandler,
    dispatcherProvider: CoroutinesDispatcherProvider
) : WebLinkViewModel(accountManager, getPointFromGeocacheCode, exceptionHandler, dispatcherProvider) {

    override fun getWebLink(point: Point): Uri {
        val cacheId = ReferenceCode.toId(point.gcData.cacheID)

        return if (BuildConfig.GEOCACHING_API_STAGING) {
            Uri.parse(String.format(Locale.ROOT, URL_FORMAT_STAGING, cacheId))
        } else {
            Uri.parse(String.format(Locale.ROOT, URL_FORMAT, cacheId))
        }
    }

    companion object {
        private const val URL_FORMAT = "https://www.geocaching.com/my/watchlist.aspx?w=%d"
        private const val URL_FORMAT_STAGING = "https://staging.geocaching.com/my/watchlist.aspx?w=%d"
    }
}
