package com.arcao.geocaching.api.oauth.services;

import org.apache.commons.io.IOUtils;
import org.scribe.exceptions.OAuthConnectionException;
import org.scribe.services.TimestampServiceImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Date;
import java.util.Random;

import timber.log.Timber;

public class ServerTimestampServiceImpl extends TimestampServiceImpl {
	private final Random rand = new Random();
	private final long ts;

	public ServerTimestampServiceImpl(URI uri) {
		ts = getServerDate(uri).getTime() / 1000;
	}

	@Override
	public String getTimestampInSeconds() {
		return String.valueOf(ts);
	}

	@Override
	public String getNonce() {
		return String.valueOf(ts + rand.nextInt());
	}

	private static Date getServerDate(URI uri) {
		InputStream is = null;
		try {
			Timber.i("Getting server time from url: " + uri);
			HttpURLConnection c = (HttpURLConnection) uri.toURL().openConnection();
			c.setRequestMethod("GET");
			c.connect();
			if (c.getResponseCode() == HttpURLConnection.HTTP_OK) {
				long date = c.getDate();
				is = c.getInputStream();
				Timber.i("Response: " + IOUtils.toString(is));
				if (date > 0) {
					Timber.i("We got time: " + date);
					return new Date(date);
				}
			}
		} catch (IOException e) {
			throw new OAuthConnectionException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}

		Timber.e("No Date header found in a response, used device time instead.");
		return new Date();
	}
}
