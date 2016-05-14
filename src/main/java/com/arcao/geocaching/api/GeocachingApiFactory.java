package com.arcao.geocaching.api;

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration;
import com.arcao.geocaching.api.downloader.OkHttpClientJsonDownloader;
import com.arcao.geocaching.api.impl.LiveGeocachingApi;
import com.arcao.geocaching.api.impl.live_geocaching_api.downloader.JsonDownloader;
import com.arcao.geocaching4locus.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class GeocachingApiFactory {
	private static GeocachingApiConfiguration apiConfiguration;
	private static OkHttpClient client;
	private static JsonDownloader jsonDownloader;

	public static GeocachingApi create() {
		return LiveGeocachingApi.Builder.liveGeocachingApi()
				.withConfiguration(getApiConfiguration())
				.withDownloader(getJsonDownloader())
				.build();
	}

	public static JsonDownloader getJsonDownloader() {
		if (jsonDownloader == null) {
			jsonDownloader = new OkHttpClientJsonDownloader(getOkHttpClient());
		}
		return jsonDownloader;
	}

	private static OkHttpClient getOkHttpClient() {
		if (client == null) {
			GeocachingApiConfiguration apiConfiguration = getApiConfiguration();
			client = new OkHttpClient.Builder()
					.connectTimeout(apiConfiguration.getConnectTimeout(), TimeUnit.MILLISECONDS)
					.readTimeout(apiConfiguration.getReadTimeout(), TimeUnit.MILLISECONDS)
					.build();
		}
		return client;
	}

	private static GeocachingApiConfiguration getApiConfiguration() {
		if (apiConfiguration == null) {
			apiConfiguration = new DefaultProductionGeocachingApiConfiguration();
			if (BuildConfig.GEOCACHING_API_STAGING)
				apiConfiguration = new DefaultStagingGeocachingApiConfiguration();
		}
		return apiConfiguration;
	}
}
