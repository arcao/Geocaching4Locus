package com.arcao.geocaching4locus.data.api.model.request.query.filter

class IsActiveFilter(val value: Boolean) : Filter {
    override fun isValid() = true

    override fun toString() = "ia:$value"
}
