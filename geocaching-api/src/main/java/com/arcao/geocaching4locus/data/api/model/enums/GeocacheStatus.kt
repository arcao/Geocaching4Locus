package com.arcao.geocaching4locus.data.api.model.enums

enum class GeocacheStatus(override val id: Int, override val value: String) : IdValueType {
    UNPUBLISHED(1, "Unpublished"),
    ACTIVE(2, "Active"),
    DISABLED(3, "Disabled"),
    LOCKED(4, "Locked"),
    ARCHIVED(5, "Archived");

    companion object {
        fun from(value: String?) = values().find { it.value == value } ?: ACTIVE
    }
}