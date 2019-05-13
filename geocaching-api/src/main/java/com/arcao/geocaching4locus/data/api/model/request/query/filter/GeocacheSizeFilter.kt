package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.enum.GeocacheSize

class GeocacheSizeFilter(private vararg val values: GeocacheSize, private var not : Boolean = false) : NotFilter<GeocacheSizeFilter> {
    override fun isValid() = values.isNotEmpty()

    override fun not(): GeocacheSizeFilter {
        not = true
        return this
    }

    override fun toString(): String {
        if (values.isEmpty()) return ""

        val ids = values.toSet().joinToString(",") {
            it.id.toString()
        }

        return if (not) {
            "type:not($ids)"
        } else {
            "type:$ids"
        }
    }
}