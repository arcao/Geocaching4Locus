package com.arcao.geocaching4locus.import_bookmarks.fragment

import androidx.fragment.app.Fragment

abstract class BaseBookmarkFragment : Fragment() {
    abstract fun onProgressCancel(requestId: Int)
}