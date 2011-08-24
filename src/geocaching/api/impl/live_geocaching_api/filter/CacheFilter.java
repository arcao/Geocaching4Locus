package geocaching.api.impl.live_geocaching_api.filter;

import geocaching.api.impl.live_geocaching_api.builder.JsonSerializable;

public interface CacheFilter extends JsonSerializable {
	public abstract String getName();
	public abstract boolean isValid();
}
