package com.arcao.geocaching4locus.data.provider;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ProviderFactory {
	private static Set<Provider> providers = new HashSet<>();
	static {
		// add all available providers here
	}

	public static Set<Provider> getProviders() {
		return Collections.unmodifiableSet(providers);
	}
}
