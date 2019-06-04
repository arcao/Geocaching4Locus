package com.arcao.geocaching4locus.data.api.model.request.query.filter

class NameFilter(private val startWith: String) : Filter {
    override fun isValid() = startWith.isNotEmpty()

    override fun toString() = "name:$startWith"
}
