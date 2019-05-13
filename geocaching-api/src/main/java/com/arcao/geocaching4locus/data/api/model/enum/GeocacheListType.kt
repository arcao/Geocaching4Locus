package com.arcao.geocaching4locus.data.api.model.enum

enum class GeocacheListType(override val id: Int, override val value: String) : IdValueType {
    POCKET_QUERY(1, "pq"),
    BOOKMARK(2, "bm"),
    IGNORE(3, "il"),
    WATCH(4, "wl"),
    FAVORITES(5, "fl");

    companion object {
        fun from(id: Int?) = values().find { it.id == id } ?: BOOKMARK
    }

    override fun toString(): String {
        return value
    }
}