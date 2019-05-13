package com.arcao.geocaching4locus.data.api.model.request.query.filter

class DifficultyFilter(private val min : Float, private val max : Float) : Filter {
    override fun isValid() = min >= 1F && max <= 5F && min <= max

    override fun toString(): String {
        return "diff:$min-$max"
    }
}