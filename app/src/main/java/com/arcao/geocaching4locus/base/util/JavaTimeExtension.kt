package com.arcao.geocaching4locus.base.util

import org.threeten.bp.Instant
import java.util.Date

fun Instant.toDate() : Date = Date(this.toEpochMilli())