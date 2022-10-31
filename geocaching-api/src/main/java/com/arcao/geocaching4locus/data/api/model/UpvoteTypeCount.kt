package com.arcao.geocaching4locus.data.api.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpvoteTypeCount(
    val upvoteTypeId: Int,
    val upvoteTypeName: String,
    val count: Int,
    val upvotedByUser: Boolean
) {
    companion object {
        const val GREAT_STORY = 1
        const val HELPFUL = 2
    }
}
