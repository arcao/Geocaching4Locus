package com.arcao.geocaching4locus.data.api.endpoint

import com.arcao.geocaching4locus.data.api.internal.Factory
import com.arcao.geocaching4locus.data.api.internal.retrofit.adapter.GeocachingApiCoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class WherigoApiEndpointFactory constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) : Factory<WherigoApiEndpoint> {
    companion object {
        private const val ENDPOINT_URL = "https://wherigo-service.appspot.com/"
    }

    override fun create(): WherigoApiEndpoint {
        return Retrofit.Builder().apply {
            client(okHttpClient)
            baseUrl(ENDPOINT_URL)
            addCallAdapterFactory(GeocachingApiCoroutineCallAdapterFactory.create())
            addConverterFactory(MoshiConverterFactory.create(moshi))
        }.build().create(WherigoApiEndpoint::class.java)
    }
}
