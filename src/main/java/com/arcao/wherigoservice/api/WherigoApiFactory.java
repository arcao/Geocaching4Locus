package com.arcao.wherigoservice.api;

import com.arcao.geocaching.api.configuration.GeocachingApiConfiguration;
import com.arcao.geocaching.api.configuration.impl.DefaultProductionGeocachingApiConfiguration;
import com.arcao.geocaching.api.downloader.OkHttpClientJsonDownloader;
import com.arcao.geocaching.api.impl.live_geocaching_api.downloader.JsonDownloader;
import com.squareup.okhttp.OkHttpClient;

public final class WherigoApiFactory {
  public static WherigoService create() {
    GeocachingApiConfiguration apiConfiguration = new DefaultProductionGeocachingApiConfiguration();
    JsonDownloader jsonDownloader = new OkHttpClientJsonDownloader(apiConfiguration, new OkHttpClient());
    return new WherigoServiceImpl(jsonDownloader);
  }
}
