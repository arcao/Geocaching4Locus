package com.arcao.geocaching4locus.data.api.model.request.query.filter

class HiddenByFilter(private val name: String, private var not: Boolean = false) : NotFilter<HiddenByFilter> {
    override fun isValid() = name.isNotEmpty()

    override fun not(): HiddenByFilter {
        not = true
        return this
    }

    override fun toString() = if (not) {
        "hby:not($name)"
    } else {
        "hby:$name"
    }
}