package com.arcao.geocaching4locus.data.api.model.request.query.filter

class FavoritePointsFilter(private val minPoints: Int) : Filter {
    override fun isValid() = minPoints > 0

    override fun toString() = "fav:$minPoints"
}
