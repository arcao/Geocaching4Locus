package com.arcao.geocaching.api;

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration;
import com.arcao.geocaching.api.downloader.Downloader;
import com.arcao.geocaching.api.downloader.OkHttpClientDownloader;
import com.arcao.geocaching4locus.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class GeocachingApiFactory {
    private static GeocachingApiConfiguration apiConfiguration;
    private static OkHttpClient client;
    private static Downloader downloader;

    public static GeocachingApi create() {
        return LiveGeocachingApi.builder()
                .configuration(getApiConfiguration())
                .downloader(getDownloader())
                .build();
    }

    public static Downloader getDownloader() {
        if (downloader == null) {
            downloader = new OkHttpClientDownloader(getOkHttpClient());
        }
        return downloader;
    }

    public static OkHttpClient getOkHttpClient() {
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
