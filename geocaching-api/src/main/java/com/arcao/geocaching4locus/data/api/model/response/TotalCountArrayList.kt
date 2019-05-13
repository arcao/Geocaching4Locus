package com.arcao.geocaching4locus.data.api.model.response

class TotalCountArrayList<T> : ArrayList<T>, MutableTotalCountList<T> {
    override var totalCount: Long = 0

    constructor() : super()
    constructor(initialCapacity : Int) : super(initialCapacity)
    constructor(c: MutableCollection<out T>?) : super(c)

    override fun toString(): String {
        return super.toString() + " (totalCount: $totalCount)"
    }
}