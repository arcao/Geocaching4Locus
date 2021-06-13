package com.arcao.geocaching4locus.data.api.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GeocacheSize(
    id: Int,
    name: String
) : Size(id, name) {
    companion object {
        const val NOT_CHOSEN = 1
        const val MICRO = 2
        const val SMALL = 8
        const val REGULAR = 3
        const val LARGE = 4
        const val VIRTUAL = 5
        const val OTHER = 6
    }
}
