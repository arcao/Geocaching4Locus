package com.arcao.geocaching4locus.data.api.model.request.query.filter

class FoundByFilter(private val name: String = "me", private var not: Boolean = false) : NotFilter<FoundByFilter> {
    override fun isValid() = name.isNotEmpty()

    override fun not(): FoundByFilter {
        not = true
        return this
    }

    override fun toString() = if (not) {
        "fby:not($name)"
    } else {
        "fby:$name"
    }
}
