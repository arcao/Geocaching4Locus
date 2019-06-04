package com.arcao.geocaching4locus.data.api.model.response

class PagedArrayList<T> : ArrayList<T>, MutablePagedList<T> {
    override var totalCount: Long = 0

    constructor() : super()

    constructor(initialCapacity : Int, totalCount : Long) : super(initialCapacity) {
        this.totalCount = totalCount
    }

    override fun toString(): String {
        return super.toString() + " (totalCount: $totalCount)"
    }
}