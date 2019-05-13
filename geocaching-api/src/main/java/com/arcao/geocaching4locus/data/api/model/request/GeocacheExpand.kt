package com.arcao.geocaching4locus.data.api.model.request

import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_GEOCACHE_LOGS
import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_IMAGES
import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_SEPARATOR
import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_TRACKABLES
import com.arcao.geocaching4locus.data.api.model.request.Expand.Companion.EXPAND_FIELD_USER_WAYPOINTS

class GeocacheExpand : Expand<GeocacheExpand> {
    var geocacheLogs: Int? = null
    var trackables: Int? = null
    var userWaypoint = false
    var images: Int? = null

    override fun all(): GeocacheExpand {
        geocacheLogs = 0
        trackables = 0
        userWaypoint = true
        images = 0
        return this
    }

    override fun toString(): String {
        val items = ArrayList<String>(4)

        geocacheLogs?.run {
            items.add(EXPAND_FIELD_GEOCACHE_LOGS.expand(this))
        }
        trackables?.run {
            items.add(EXPAND_FIELD_TRACKABLES.expand(this))
        }
        if (userWaypoint) items.add(EXPAND_FIELD_USER_WAYPOINTS)
        images?.run {
            items.add(EXPAND_FIELD_IMAGES.expand(this))
        }

        return items.joinToString(EXPAND_FIELD_SEPARATOR)
    }
}
