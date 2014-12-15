package com.arcao.geocaching4locus.data.provider.callback;

import locus.api.objects.extra.Waypoint;

public class DataReceivedCallback implements Callback {
	private final Waypoint[] data;

	public DataReceivedCallback(Waypoint[] data) {
		this.data = data;
	}

	public Waypoint[] getData() {
		return data;
	}
}
