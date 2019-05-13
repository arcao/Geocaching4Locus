package com.arcao.geocaching4locus.data.api.model.request.query.filter

class HasCorrectedCoordinatesFilter : Filter {
    override fun isValid() = true

    override fun toString() = "hcc"
}