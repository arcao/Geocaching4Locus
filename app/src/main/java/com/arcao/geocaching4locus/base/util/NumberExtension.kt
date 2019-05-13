package com.arcao.geocaching4locus.base.util

fun Double.whenNaN(value: Double) = if (isNaN()) value else this