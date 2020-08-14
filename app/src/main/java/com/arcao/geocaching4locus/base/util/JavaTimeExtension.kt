package com.arcao.geocaching4locus.base.util

import java.time.Instant
import java.util.Date

fun Instant.toDate(): Date = Date(this.toEpochMilli())
