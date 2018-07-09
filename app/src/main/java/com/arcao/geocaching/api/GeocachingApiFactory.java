package com.arcao.geocaching.api;

import android.os.Build;

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultStagingGeocachingApiConfiguration;
import com.arcao.geocaching.api.downloader.Downloader;
import com.arcao.geocaching.api.downloader.OkHttpClientDownloader;
import com.arcao.geocaching4locus.BuildConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import timber.log.Timber;

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
            client = enableTls12OnPreLollipop(new OkHttpClient.Builder()
                    .connectTimeout(apiConfiguration.getConnectTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(apiConfiguration.getReadTimeout(), TimeUnit.MILLISECONDS)
            ).build();
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

    private static OkHttpClient.Builder enableTls12OnPreLollipop(OkHttpClient.Builder client) {
        if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
            try {
                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(ConnectionSpec.MODERN_TLS);
                specs.add(ConnectionSpec.COMPATIBLE_TLS);
                specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);
            } catch (Exception e) {
                Timber.e(e, "Error while setting TLS 1.2");
            }
        }

        return client;
    }
}
