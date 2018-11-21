package com.arcao.geocaching4locus.weblink

import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class BookmarkGeocacheWebLinkActivity : WebLinkActivity() {
    override val viewModel by viewModel<BookmarkGeocacheWebLinkViewModel>()
}