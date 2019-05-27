package com.arcao.geocaching4locus.data.api.model.response

internal class PagedArrayList<T> : ArrayList<T>, MutablePagedList<T> {
    override var totalCount: Long = 0

    constructor() : super()

    override fun toString(): String {
        return super.toString() + " (totalCount: $totalCount)"
    }
}