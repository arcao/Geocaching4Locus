package com.arcao.geocaching4locus.data.api.model.enum

enum class TrackableLogType(override val id: Int, override val value: String) : IdValueType {
    WRITE_NOTE(4, "Write Note"),
    RETRIEVE_IT_FROM_A_CACHE(13, "Retrieve It from a Cache"),
    DROPPED_OFF(14, "Dropped Off"),
    TRANSFER(15, "Transfer"),
    MARK_MISSING(16, "Mark Missing"),
    GRAB_IT(19, "Grab It (Not from a Cache)"),
    DISCOVERED_IT(48, "Discovered It"),
    MOVE_TO_COLLECTION(69, "Move to Collection"),
    MOVE_TO_INVENTORY(70, "Move to Inventory"),
    VISITED(75, "Visited");

    companion object {
        fun from(value: String?) = values().find { it.value == value } ?: WRITE_NOTE
    }
}