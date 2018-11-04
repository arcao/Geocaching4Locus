package com.arcao.geocaching4locus.weblink

import org.koin.android.viewmodel.ext.android.viewModel

class BookmarkGeocacheWebLinkActivity : WebLinkActivity() {
    override val viewModel by viewModel<BookmarkGeocacheWebLinkViewModel>()
}