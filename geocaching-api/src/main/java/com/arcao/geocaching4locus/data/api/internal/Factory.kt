package com.arcao.geocaching4locus.data.api.internal

interface Factory<T> {
    fun create() : T
}