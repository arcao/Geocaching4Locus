package com.arcao.geocaching4locus.data.api.model.request

import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_IMAGES

class GeocacheTrackableExpand : Expand<GeocacheTrackableExpand> {
    var images: Int? = null

    override fun all(): GeocacheTrackableExpand {
        images = 0
        return this
    }

    override fun toString(): String {
        return images?.run { EXPAND_FIELD_IMAGES.expand(this) } ?: ""
    }
}
