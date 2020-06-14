package com.arcao.geocaching4locus.data.api.internal.moshi

import com.arcao.geocaching4locus.data.api.internal.Factory
import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.EnumAdapterFactory
import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.Java8TimeAdapter
import com.arcao.geocaching4locus.data.api.internal.moshi.adapter.PagedListAdapter
import com.squareup.moshi.Moshi

object MoshiFactory : Factory<Moshi> {
    override fun create(): Moshi =
        Moshi.Builder().apply {
            add(Java8TimeAdapter())
            add(EnumAdapterFactory())
            add(PagedListAdapter.FACTORY)
        }.build()
}
