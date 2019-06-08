package com.arcao.geocaching4locus.data.api.model.request.query.filter

class GeocacheTypeFilter(private vararg val values: Int, private var not: Boolean = false) :
    NotFilter<GeocacheTypeFilter> {
    override fun isValid() = values.isNotEmpty()

    override fun not(): GeocacheTypeFilter {
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
