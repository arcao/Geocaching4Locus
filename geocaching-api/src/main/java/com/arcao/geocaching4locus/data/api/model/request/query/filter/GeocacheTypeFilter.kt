package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.enum.GeocacheType

class GeocacheTypeFilter(private vararg val values: GeocacheType, private var not : Boolean = false) : NotFilter<GeocacheTypeFilter> {
    override fun isValid() = values.isNotEmpty()

    override fun not(): GeocacheTypeFilter {
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