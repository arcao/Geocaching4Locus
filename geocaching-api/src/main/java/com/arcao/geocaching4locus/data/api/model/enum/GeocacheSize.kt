package com.arcao.geocaching4locus.data.api.model.enum

enum class GeocacheSize(override val id: Int, override val value: String) : IdValueType {
    NOT_CHOSEN(1, "Not chosen"),
    MICRO(2, "Micro"),
    SMALL(8, "Small"),
    MEDIUM(3, "Medium"),
    LARGE(4, "Large"),
    VIRTUAL(5, "Virtual"),
    OTHER(6, "Other");

    companion object {
        fun from(value: String?) = values().find { it.value == value } ?: NOT_CHOSEN
    }
}