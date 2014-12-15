package com.arcao.geocaching4locus.data.provider;

import com.arcao.geocaching4locus.data.provider.impl.geocaching_live_api.GeocachingLiveApiProvider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ProviderFactory {
	private static final Set<Provider> providers = new HashSet<>();
	static {
		providers.add(new GeocachingLiveApiProvider());
	}

	public static Set<Provider> getProviders() {
		return Collections.unmodifiableSet(providers);
	}

	public static Provider getProviderForCacheCode(String cacheCode) {
		for (Provider provider : providers) {
			if (provider.canHandleCacheCode(cacheCode))
				return provider;
		}

		return null;
	}
}
