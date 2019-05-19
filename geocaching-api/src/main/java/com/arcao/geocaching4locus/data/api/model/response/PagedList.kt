package com.arcao.geocaching4locus.data.api.model.response

interface PagedList<T> : List<T> {
    val totalCount : Long
}

internal interface MutablePagedList<T> : PagedList<T>, MutableList<T> {
    override var totalCount: Long
}