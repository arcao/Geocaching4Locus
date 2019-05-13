package com.arcao.geocaching4locus.data.api.model.request.query.filter

interface NotFilter<T : NotFilter<T>> : Filter {
    fun not(): T
}