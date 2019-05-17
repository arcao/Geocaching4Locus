package com.arcao.geocaching4locus.data.api.model.request.query.filter

class GeocacheSizeFilter(private vararg val values: Int, private var not : Boolean = false) : NotFilter<GeocacheSizeFilter> {
    override fun isValid() = values.isNotEmpty()

    override fun not(): GeocacheSizeFilter {
        not = true
        return this
    }

    override fun toString(): String {
        if (values.isEmpty()) return ""

        val ids = values.joinToString(",")

        return if (not) {
            "type:not($ids)"
        } else {
            "type:$ids"
        }
    }
}