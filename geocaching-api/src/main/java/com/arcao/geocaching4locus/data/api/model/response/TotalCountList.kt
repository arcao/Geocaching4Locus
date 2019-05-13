package com.arcao.geocaching4locus.data.api.model.response

interface TotalCountList<T> : List<T> {
    val totalCount : Long
}

interface MutableTotalCountList<T> : TotalCountList<T>, MutableList<T> {
    override var totalCount: Long
}