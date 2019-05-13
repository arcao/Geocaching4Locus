package com.arcao.geocaching4locus.data.api.model.request

enum class GeocacheSort(val value: String) {
    DISTANCE_ASC("distance+"),
    DISTANCE_DESC("distance-"),
    FAVORITES_ASC("favorites+"),
    FAVORITES_DESC("favorites-"),
    CACHE_NAME_ASC("cachename+"),
    CACHE_NAME_DESC("cachename-"),
    ID_ASC("id+"),
    ID_DESC("id-");

    override fun toString(): String {
        return value
    }
}