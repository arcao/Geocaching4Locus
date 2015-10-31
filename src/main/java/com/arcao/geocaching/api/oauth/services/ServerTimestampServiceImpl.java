package com.arcao.geocaching.api.oauth.services;

import com.arcao.wherigoservice.api.WherigoApiFactory;
import java.util.Random;
import org.scribe.services.TimestampServiceImpl;
import timber.log.Timber;

public class ServerTimestampServiceImpl extends TimestampServiceImpl {
	private final Random rand = new Random();
	private final long ts;

	public ServerTimestampServiceImpl() {
		long time;
		try {
			time = WherigoApiFactory.create().getTime();
			Timber.i("server time received (ms): " + time);
		} catch (Exception e) {
			time = System.currentTimeMillis();
			Timber.e(e, "No server time received. Used system time (ms): " + time);
		}

		// timestamp in seconds
		ts = time / 1000;
	}

	@Override
	public String getTimestampInSeconds() {
		return String.valueOf(ts);
	}

	@Override
	public String getNonce() {
		return String.valueOf(ts + rand.nextInt());
	}
}
