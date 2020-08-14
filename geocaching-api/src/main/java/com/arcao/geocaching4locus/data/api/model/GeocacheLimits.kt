package com.arcao.geocaching4locus.data.api.model

import com.squareup.moshi.JsonClass
import java.time.Duration

@JsonClass(generateAdapter = true)
data class GeocacheLimits(
    val liteCallsRemaining: Int, // 0
    val liteCallsSecondsToLive: Duration?, // 0
    val fullCallsRemaining: Int, // 0
    val fullCallsSecondsToLive: Duration? // 0
)
