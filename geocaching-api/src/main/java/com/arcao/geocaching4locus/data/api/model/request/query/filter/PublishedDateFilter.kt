package com.arcao.geocaching4locus.data.api.model.request.query.filter

import org.threeten.bp.LocalDate

class PublishedDateFilter(private val start: LocalDate, private val end: LocalDate?) : Filter {
    override fun isValid() = true

    override fun toString() = "pb:[$start," + (end ?: "") + "]"
}
