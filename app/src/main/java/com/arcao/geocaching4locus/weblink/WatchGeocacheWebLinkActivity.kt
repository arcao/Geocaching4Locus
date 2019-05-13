package com.arcao.geocaching4locus.weblink

import org.koin.androidx.viewmodel.ext.android.viewModel

class WatchGeocacheWebLinkActivity : WebLinkActivity() {
    override val viewModel by viewModel<WatchGeocacheWebLinkViewModel>()
}
