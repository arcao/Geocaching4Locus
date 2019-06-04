package com.arcao.geocaching4locus.data.api.model.request.query.filter

class ProvinceFilter(val name: String) : Filter {
    override fun isValid() = name.isNotEmpty()

    override fun toString() = "prov:$name"
}