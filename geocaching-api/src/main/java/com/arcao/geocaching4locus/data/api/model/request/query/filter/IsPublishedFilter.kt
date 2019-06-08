package com.arcao.geocaching4locus.data.api.model.request.query.filter

class IsPublishedFilter(val value: Boolean) : Filter {
    override fun isValid() = true

    override fun toString() = "ip:$value"
}
