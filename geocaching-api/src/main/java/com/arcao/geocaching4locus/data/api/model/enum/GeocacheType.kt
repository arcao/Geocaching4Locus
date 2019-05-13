package com.arcao.geocaching4locus.data.api.model.enum

enum class GeocacheType(override val id: Int, override val value: String) : IdValueType {
    TRADITIONAL(2, "Traditional"),
    MULTI_CACHE(3, "Multi-Cache"),
    VIRTUAL(4, "Virtual"),
    LETTERBOX_HYBRID(5, "Letterbox Hybrid"),
    EVENT(6, "Event"),
    MYSTERY_UNKNOWN(8, "Mystery/Unknown"),
    PROJECT_APE(9, "Project A.P.E."),
    WEBCAM(11, "Webcam"),
    CACHE_IN_TRASH_OUT_EVENT(13, "Cache In Trash Out Event"),
    EARTHCACHE(137, "Earthcache"),
    MEGA_EVENT(453, "Mega-Event"),
    GPS_ADVENTURES_EXHIBIT(1304, "GPS Adventures Exhibit"),
    WHERIGO(1858, "Wherigo"),
    GEOCACHING_HQ(3773, "Geocaching HQ"),
    GIGA_EVENT(7005, "Giga-Event");

    companion object {
        fun from(value: String?) = values().find { it.value == value } ?: MYSTERY_UNKNOWN
    }
}