package com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api;

import android.content.Context;

import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.impl.LiveGeocachingApiFactory;
import com.arcao.geocaching4locus.data.provider.ProviderService;
import com.arcao.geocaching4locus.data.provider.callback.CallbackListener;
import com.arcao.geocaching4locus.data.provider.callback.DataReceivedCallback;
import com.arcao.geocaching4locus.data.provider.exception.AccountNotFoundProviderException;
import com.arcao.geocaching4locus.data.provider.exception.ProviderException;
import com.arcao.geocaching4locus.data.provider.exception.ProviderExceptionCategory;
import com.arcao.geocaching4locus.util.Coordinates;

import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;

public class GeocachingLiveApiProviderService implements ProviderService, GeocachingLiveApiKeys {
	private final GeocachingLiveApiConfiguration configuration;
	private final Context context;

	public GeocachingLiveApiProviderService(Context context) {
		this.context = context;

		configuration = new GeocachingLiveApiConfiguration(context);
	}

	private GeocachingApi createApi() throws ProviderException {
		try {
			GeocachingApi api = LiveGeocachingApiFactory.getLiveGeocachingApi();

			String token = configuration.getToken();
			if (token == null) {
				configuration.removeAccount();
				throw new AccountNotFoundProviderException("Account not found.");
			}

			api.openSession(token);
			return api;
		} catch (GeocachingApiException e) {
			throw mapException(e);
		}
	}

	@Override
	public boolean isAvailable() {
		return configuration.isAccountValid();
	}

	@Override
	public void retrieveGeocache(String gcCode, CallbackListener listener) throws ProviderException {
		GeocachingApi api = createApi();

		try {
			Geocache cache = api.getCache(gcCode, configuration.getGeocacheLogCount(), 0);
			listener.onCallback(new DataReceivedCallback(new Waypoint[]{LocusDataMapper.toLocusPoint(context, cache)}));
		} catch (GeocachingApiException e) {
			throw mapException(e);
		}
	}

	@Override
	public void retrieveGeocaches(Waypoint[] originalWaypoints, CallbackListener listener) throws ProviderException {

	}

	@Override
	public void searchNearest(Coordinates coordinates, CallbackListener listener) throws ProviderException {

	}

	@Override
	public void searchLiveMap(Coordinates topLeft, Coordinates bottomRight, CallbackListener listener) throws ProviderException {

	}

	private ProviderException mapException(GeocachingApiException e) {
		return new ProviderException(e.getMessage(), ProviderExceptionCategory.OTHER, e);
	}
}
