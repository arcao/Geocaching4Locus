package com.arcao.geocaching.api;

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration;
import com.arcao.geocaching.api.downloader.OkHttpClientJsonDownloader;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching.api.impl.live_geocaching_api.downloader.JsonDownloader;
import com.arcao.geocaching4locus.BuildConfig;
import com.squareup.okhttp.OkHttpClient;

public class GeocachingApiFactory {
	public static GeocachingApi create() {
		GeocachingApiConfiguration apiConfiguration = new DefaultProductionGeocachingApiConfiguration();
		if (BuildConfig.GEOCACHING_API_STAGING)
			apiConfiguration = new DefaultStagingGeocachingApiConfiguration();

		JsonDownloader jsonDownloader = new OkHttpClientJsonDownloader(apiConfiguration, new OkHttpClient());
		return LiveGeocachingApi.Builder.liveGeocachingApi().withConfiguration(apiConfiguration).withDownloader(jsonDownloader).build();
	}
}
