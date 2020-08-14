package com.arcao.geocaching4locus.data.api.model.request.query

import com.arcao.geocaching4locus.data.api.model.request.query.filter.Filter

class GeocacheQuery {
    private val filters = mutableSetOf<Filter>()

    fun add(vararg filters: Filter): GeocacheQuery {
        this.filters.addAll(filters.filter(Filter::isValid))
        return this
    }

    override fun toString(): String = filters.joinToString("+")
}

fun queryOf(vararg filters: Filter) = GeocacheQuery().add(*filters)
