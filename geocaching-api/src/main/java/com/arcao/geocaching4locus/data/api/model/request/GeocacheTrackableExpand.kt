package com.arcao.geocaching4locus.data.api.model.request

import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_IMAGES
import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_TRACKABLE_LOGS
import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_TRACKABLE_LOG_IMAGES

class GeocacheTrackableExpand : Expand<GeocacheTrackableExpand> {
    var images: Int? = null
    var trackableLogs: Int? = null
    var trackableLogsImages: Int? = null

    override fun all(): GeocacheTrackableExpand {
        images = 0
        trackableLogs = 0
        trackableLogsImages = 0
        return this
    }

    override fun toString(): String {
        val items = ArrayList<String>(3)

        images?.run {
            items.add(EXPAND_FIELD_IMAGES.expand(this))
        }
        trackableLogs?.run {
            items.add(EXPAND_FIELD_TRACKABLE_LOGS.expand(this))
        }
        trackableLogsImages?.run {
            items.add(EXPAND_FIELD_TRACKABLE_LOG_IMAGES.expand(this))
        }

        return items.joinToString(Expand.EXPAND_FIELD_SEPARATOR)
    }
}
