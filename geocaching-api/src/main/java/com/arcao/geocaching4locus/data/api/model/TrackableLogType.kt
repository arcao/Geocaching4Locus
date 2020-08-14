package com.arcao.geocaching4locus.data.api.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class TrackableLogType(
    id: Int,
    name: String,
    imageUrl: String
) : Type(id, name, imageUrl) {
    companion object {
        const val WRITE_NOTE = 4
        const val RETRIEVE_IT_FROM_A_CACHE = 13
        const val DROPPED_OFF = 14
        const val TRANSFER = 15
        const val MARK_MISSING = 16
        const val GRAB_IT = 19
        const val DISCOVERED_IT = 48
        const val MOVE_TO_COLLECTION = 69
        const val MOVE_TO_INVENTORY = 70
        const val VISITED = 75
    }
}
