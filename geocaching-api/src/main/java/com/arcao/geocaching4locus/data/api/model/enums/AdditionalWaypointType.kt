package com.arcao.geocaching4locus.data.api.model.enums

enum class AdditionalWaypointType(override val id: Int, override val value: String) : IdValueType {
    PARKING_AREA(217, "Parking Area"),
    VIRTUAL_STAGE(218, "Virtual Stage"),
    PHYSICAL_STAGE(219, "Physical Stage"),
    FINAL_LOCATION(220, "Final Location"),
    TRAILHEAD(221, "Trailhead"),
    REFERENCE_POINT(452, "Reference Point");

    companion object {
        fun from(id: Int?) = values().find { it.id == id } ?: REFERENCE_POINT
    }
}