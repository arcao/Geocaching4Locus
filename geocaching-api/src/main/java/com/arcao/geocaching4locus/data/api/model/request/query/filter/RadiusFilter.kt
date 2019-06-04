package com.arcao.geocaching4locus.data.api.model.request.query.filter

class RadiusFilter(private val distance: Int, private val unit : DistanceUnit) : Filter {
    override fun isValid() = distance > 0

    override fun toString(): String {
        return "radius:$distance${unit.value}"
    }
}

enum class DistanceUnit(val value : String) {
    KILOMETER("km"),
    MILE("mi"),
    METER("m")
}