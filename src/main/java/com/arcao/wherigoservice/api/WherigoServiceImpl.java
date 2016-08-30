package com.arcao.wherigoservice.api;

import com.arcao.geocaching.api.exception.InvalidResponseException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.downloader.JsonDownloader;
import com.arcao.geocaching.api.parser.JsonReader;
import com.arcao.wherigoservice.api.parser.WherigoJsonResultParser;
import com.arcao.wherigoservice.api.parser.WherigoJsonResultParser.Result;
import com.google.gson.stream.MalformedJsonException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import timber.log.Timber;

public class WherigoServiceImpl implements WherigoService {
	private static final Logger logger = LoggerFactory.getLogger(WherigoServiceImpl.class);

	private static final String BASE_URL = "https://wherigo-service.appspot.com/api";
	private final JsonDownloader downloader;

	public WherigoServiceImpl(JsonDownloader downloader) {
		this.downloader = downloader;
	}

	@Override
	public String getCacheCodeFromGuid(String cacheGuid) throws WherigoServiceException {
		String cacheCode = null;

		try {
			JsonReader r = callGet("getCacheCodeFromGuid?CacheGUID=" + cacheGuid +
					"&format=json");

			r.beginObject();
			checkError(r);

			while (r.hasNext()) {
				String name = r.nextName();
				if ("CacheResult".equals(name)) {
					r.beginObject();
					while (r.hasNext()) {
						name = r.nextName();
						if ("CacheCode".equals(name)) {
							cacheCode = r.nextString();
						} else {
							r.skipValue();
						}
					}
					r.endObject();
				} else {
					r.skipValue();
				}
			}
			r.endObject();
			r.close();
			Timber.i("Cache code: " + cacheCode);

		} catch (NetworkException e) {
			throw new WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.getMessage(), e);
		} catch (InvalidResponseException e) {
			throw new WherigoServiceException(WherigoServiceException.ERROR_API_ERROR, "Response is not valid JSON string: " + e.getMessage(), e);
		} catch (IOException e) {
			Timber.e(e, e.toString());
			if (!isGsonException(e)) {
				throw new WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.getMessage(), e);
			}

			throw new WherigoServiceException(WherigoServiceException.ERROR_API_ERROR, "Response is not valid JSON string: " + e.getMessage(), e);
		}

		return cacheCode;
	}

	@Override
	public long getTime() throws WherigoServiceException {
		long time = 0;

		try {
			JsonReader r = callGet("getTime?format=json");

			r.beginObject();
			checkError(r);

			while (r.hasNext()) {
				String name = r.nextName();
				if ("TimeResult".equals(name)) {
					r.beginObject();
					while (r.hasNext()) {
						name = r.nextName();
						if ("Time".equals(name)) {
							time = r.nextLong();
						} else {
							r.skipValue();
						}
					}
					r.endObject();
				} else {
					r.skipValue();
				}
			}
			r.endObject();
			r.close();
			Timber.i("Time: " + time);

		} catch (NetworkException e) {
			throw new WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.getMessage(), e);
		} catch (InvalidResponseException e) {
			throw new WherigoServiceException(WherigoServiceException.ERROR_API_ERROR, "Response is not valid JSON string: " + e.getMessage(), e);
		} catch (IOException e) {
			Timber.e(e, e.toString());
			if (!isGsonException(e)) {
				throw new WherigoServiceException(WherigoServiceException.ERROR_CONNECTION_ERROR, e.getMessage(), e);
			}

			throw new WherigoServiceException(WherigoServiceException.ERROR_API_ERROR, "Response is not valid JSON string: " + e.getMessage(), e);
		}

		return time;
	}

	// -------------------- Helper methods ----------------------------------------

	private void checkError(JsonReader r) throws IOException {
		if ("Status".equals(r.nextName())) {
			Result status = WherigoJsonResultParser.parse(r);

			switch (status.getStatusCode()) {
				case WherigoServiceException.ERROR_OK:
					return;
				default:
					throw new WherigoServiceException(status.getStatusCode(), status.getStatusMessage());
			}
		} else {
			throw new WherigoServiceException(WherigoServiceException.ERROR_API_ERROR, "Missing Status in a response.");
		}
	}

	protected JsonReader callGet(String function) throws NetworkException, InvalidResponseException {
		logger.debug("Getting " + maskParameterValues(function));

		try {
			URL url = new URL(BASE_URL + "/" + function);
			return downloader.get(url);
		} catch (MalformedURLException e) {
			logger.error(e.toString(), e);
			throw new NetworkException("Error while downloading data (" + e.getClass().getSimpleName() + ")", e);
		}
	}

	protected JsonReader callPost(String function, String postBody) throws NetworkException, InvalidResponseException {
		logger.debug("Posting " + maskParameterValues(function));
		logger.debug("Body: " + maskJsonParameterValues(postBody));

		try {
			byte[] postData = postBody.getBytes("UTF-8");
			URL url = new URL(BASE_URL + "/" + function);

			return downloader.post(url, postData);
		} catch (MalformedURLException e) {
			logger.error(e.toString(), e);
			throw new NetworkException("Error while downloading data (" + e.getClass().getSimpleName() + ")", e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.toString(), e);
			throw new NetworkException("Error while downloading data (" + e.getClass().getSimpleName() + "): " + e.getMessage(), e);
		}
	}

	protected String maskParameterValues(String function) {
		function = function.replaceAll("([Aa]ccess[Tt]oken=)([^&]+)", "$1******");
		return function;
	}

	protected String maskJsonParameterValues(String postBody) {
		postBody = postBody.replaceAll("(\"[Aa]ccess[Tt]oken\"\\s*:\\s*\")([^\"]+)(\")", "$1******$3");
		return postBody;
	}

	protected boolean isGsonException(Throwable t) {
		// This IOException mess will be fixed in a next GSON release
		return (IOException.class.equals(t.getClass()) && t.getMessage() != null && t.getMessage().startsWith("Expected JSON document")) || t instanceof MalformedJsonException || t instanceof IllegalStateException || t instanceof NumberFormatException;
	}
}
