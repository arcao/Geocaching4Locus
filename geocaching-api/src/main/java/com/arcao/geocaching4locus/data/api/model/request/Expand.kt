package com.arcao.geocaching4locus.data.api.model.request

interface Expand<T> {
    companion object {
        const val EXPAND_FIELD_TRACKABLES = "trackables"
        const val EXPAND_FIELD_TRACKABLE_LOGS = "trackablelogs"
        const val EXPAND_FIELD_TRACKABLE_LOG_IMAGES = "trackablelog.images"
        const val EXPAND_FIELD_GEOCACHE_LOGS = "geocachelogs"
        const val EXPAND_FIELD_IMAGES = "images"
        const val EXPAND_FIELD_GEOCACHE_LOG_IMAGES = "geocachelog.images"
        const val EXPAND_FIELD_USER_WAYPOINTS = "userwaypoints"
        const val EXPAND_FIELD_SEPARATOR = ","
    }

    fun all() : T

    fun String.expand(value: Int) = if (value != 0) "$this:$value" else this
}
