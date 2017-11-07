package com.arcao.wherigoservice.api;

import com.arcao.geocaching.api.GeocachingApiFactory;

public final class WherigoApiFactory {
    public static WherigoService create() {
        return new WherigoServiceImpl(GeocachingApiFactory.getJsonDownloader());
    }
}
