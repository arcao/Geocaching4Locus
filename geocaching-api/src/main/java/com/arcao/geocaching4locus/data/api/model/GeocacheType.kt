package com.arcao.geocaching4locus.data.api.model

class GeocacheType(
    id: Int,
    name: String,
    imageUrl: String
) : Type(id, name, imageUrl) {
    companion object {
        const val TRADITIONAL = 2
        const val MULTI_CACHE = 3
        const val VIRTUAL = 4
        const val LETTERBOX_HYBRID = 5
        const val EVENT = 6
        const val MYSTERY_UNKNOWN = 8
        const val PROJECT_APE = 9
        const val WEBCAM = 11
        const val LOCATIONLESS_CACHE = 12
        const val CACHE_IN_TRASH_OUT_EVENT = 13
        const val EARTHCACHE = 137
        const val MEGA_EVENT = 453
        const val GPS_ADVENTURES_EXHIBIT = 1304
        const val WHERIGO = 1858
        const val LOST_AND_FOUND_EVENT_CACHE = 3653
        const val GEOCACHING_HQ = 3773
        const val GEOCACHING_LOST_AND_FOUND_CELEBRATION = 3774
        const val GEOCACHING_BLOCK_PARTY = 4738
        const val GIGA_EVENT = 7005
    }
}