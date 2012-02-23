package geocaching.api.impl;

import geocaching.api.AbstractGeocachingApiV2;
import geocaching.api.GeocachingApiProgressListener;
import geocaching.api.data.CacheLog;
import geocaching.api.data.ImageData;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.TravelBug;
import geocaching.api.data.UserProfile;
import geocaching.api.data.type.LogType;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.exception.InvalidCredentialsException;
import geocaching.api.exception.InvalidSessionException;
import geocaching.api.impl.live_geocaching_api.filter.Filter;
import geocaching.api.impl.live_geocaching_api.parser.GeocacheJsonParser;
import geocaching.api.impl.live_geocaching_api.parser.JsonReader;
import geocaching.api.impl.live_geocaching_api.parser.SimpleGeocacheJsonParser;
import geocaching.api.impl.live_geocaching_api.parser.StatusJsonParser;
import google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import android.util.Log;

public class LiveGeocachingApi extends AbstractGeocachingApiV2 implements GeocachingApiProgressListener {
	private static final String TAG = "Geocaching4Locus|LiveGeocachingApi";
	
	private static final String GHOST_USERNAME = "";
	private static final String GHOST_PASSWORD = "";

	private static final String CONSUMER_KEY = "90C7F340-7998-477D-B4D3-AC48A9A0F560";
	private static final String LICENCE_KEY = "40940392-0C8E-487B-BC40-EA250D6D9AE0";
	
	private static final String BASE_URL = "https://api.groundspeak.com/LiveV5/geocaching.svc/";
	//private static final String BASE_URL = "https://staging.api.groundspeak.com/GreenesGang/Geocaching.svc/";

	@Override
	public void openSession(String userName, String password) throws GeocachingApiException {
		try {
			JsonReader r = callGet(
					"GetUserCredentials?ConsumerKey=" + CONSUMER_KEY +
					"&LicenseKey=" + LICENCE_KEY + 
					"&Username=" + URLEncoder.encode(userName, "UTF-8") +
					"&Password=" + URLEncoder.encode(password, "UTF-8") +
					"&format=json"
			);

			r.beginObject();
			checkError(r);
			
			while (r.hasNext()) {
				String name = r.nextName();
				if ("UserGuid".equals(name)) {
					session = r.nextString();
				} else {
					r.skipValue();
				}
			}
			r.endObject();
			r.close();
			Log.i(TAG, "Session: " + session);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.toString(), e);
			session = null;
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
			throw new GeocachingApiException("Response is not valid JSON string: " + e.getMessage());
		}
	}
	
	@Override
	public void openSession(String session) throws GeocachingApiException {
		if (session != null) {
			super.openSession(session);
			return;
		}
		openSession(GHOST_USERNAME, GHOST_PASSWORD);
	}

	@Override
	public void closeSession() {
		session = null;
	}

	@Override
	public boolean isSessionValid() {
		return true;
	}
	
	@Override
	public List<SimpleGeocache> searchForGeocaches(boolean isLite, int startIndex, int maxPerPage, int geocacheLogCount, int trackableLogCount,
			Filter[] filters) throws GeocachingApiException {
		
		List<SimpleGeocache> list = new ArrayList<SimpleGeocache>();
		
		try {
			StringWriter sw = new StringWriter();
			JsonWriter w = new JsonWriter(sw);
			w.beginObject();
			w.name("AccessToken").value(session);
			w.name("IsLite").value(isLite);
			w.name("StartIndex").value(startIndex);
			w.name("MaxPerPage").value(maxPerPage);
			
			if (geocacheLogCount >= 0)
				w.name("GeocacheLogCount").value(geocacheLogCount);
			
			if (trackableLogCount >= 0)
				w.name("TrackableLogCount").value(trackableLogCount);
			
			for (Filter filter : filters) {
				if (filter.isValid())
					filter.writeJson(w);
			}
			w.endObject();
			w.close();
			
			JsonReader r = callPost("SearchForGeocaches?format=json", sw.toString());
			r.beginObject();
			checkError(r);
			
			while(r.hasNext()) {
				String name = r.nextName();
				if ("Geocaches".equals(name)) {
					if (isLite) {
						list = SimpleGeocacheJsonParser.parseList(r, this);
					} else {
						list = GeocacheJsonParser.parseList(r, this);
					}
				} else {
					r.skipValue();
				}
			}
			r.endObject();
			r.close();
			return list;
		} catch (IOException e) {
			Log.e(TAG, e.toString(), e);
			throw new GeocachingApiException("Response is not valid JSON string: " + e.getMessage());
		}
	}

	@Override
	public TravelBug getTravelBug(String travelBugCode) throws GeocachingApiException {
		throw new GeocachingApiException("Not implemented.");
	}

	@Override
	public List<TravelBug> getTravelBugsByCache(String cacheCode) throws GeocachingApiException {
		throw new GeocachingApiException("Not implemented.");
	}

	@Override
	public List<CacheLog> getCacheLogs(String cacheCode, int startPosition, int endPosition) throws GeocachingApiException {
		throw new GeocachingApiException("Not implemented.");
	}
	
	@Override
	public CacheLog createFieldNoteAndPublish(String cacheCode, LogType logType, Date dateLogged, String note, boolean publish, ImageData imageData,
			boolean favoriteThisCache) throws GeocachingApiException {
		throw new GeocachingApiException("Not implemented.");
	}
	
	@Override
	public UserProfile getYourUserProfile(boolean favoritePointData, boolean geocacheData, boolean publicProfileData, boolean souvenirData, boolean trackableData) throws GeocachingApiException {
		throw new GeocachingApiException("Not implemented.");
	}
	
	// -------------------- Helper methods ----------------------------------------
	
	protected void checkError(JsonReader r) throws GeocachingApiException, IOException {
		if ("Status".equals(r.nextName())) {
			StatusJsonParser.Status status = StatusJsonParser.parse(r);
			
			switch (status.getStatusCode()) {
				case OK:
					return;
				case NotAuthorized:
				case UserAccountProblem:
				case UserDidNotAuthorize:
				case UserTokenNotValid:
					throw new InvalidSessionException(status.getStatusMessage());
				case AccountNotFound:
					throw new InvalidCredentialsException(status.getStatusMessage());
				default:
					throw new GeocachingApiException(status.getStatusMessage());
			}
		}
	}
	
	protected JsonReader callGet(String function) throws GeocachingApiException {
		InputStream is = null;
		InputStreamReader isr = null;

		Log.i(TAG, "Getting " + maskPassword(function));

		try {
			URL url = new URL(BASE_URL + function);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// important! sometimes GC API takes too long to get response
			con.setConnectTimeout(30000);
			con.setReadTimeout(30000);

			con.setRequestMethod("GET");
			//con.setRequestProperty("User-Agent", "Geocaching/4.0 CFNetwork/459 Darwin/10.0.0d3");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Language", "en-US");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");

			final String encoding = con.getContentEncoding();

			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				Log.i(TAG, "callGet(): GZIP OK");
				is = new GZIPInputStream(con.getInputStream());
			} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
				Log.i(TAG, "callGet(): DEFLATE OK");
				is = new InflaterInputStream(con.getInputStream(), new Inflater(true));
			} else {
				Log.i(TAG, "callGet(): WITHOUT COMPRESSION");
				is = con.getInputStream();
			}

			isr = new InputStreamReader(is, "UTF-8");
			return new JsonReader(isr);
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
			throw new GeocachingApiException("Error while downloading data (" + e.getClass().getSimpleName() + ")", e);
		}
	}

	protected JsonReader callPost(String function, String postBody) throws GeocachingApiException {
		InputStream is = null;
		InputStreamReader isr = null;

		Log.i(TAG, "Posting " + maskPassword(function));
		
		try {
			byte[] data = postBody.getBytes("UTF-8");
			
			URL url = new URL(BASE_URL + function);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setDoOutput(true);
			
			// important! sometimes GC API takes too long to get response
			con.setConnectTimeout(30000);
			con.setReadTimeout(30000);

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Content-Length", Integer.toString(data.length));
			//con.setRequestProperty("User-Agent", "Geocaching/4.0 CFNetwork/459 Darwin/10.0.0d3");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Language", "en-US");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");

			OutputStream os = con.getOutputStream();
						
			Log.i(TAG, "Body: " + postBody);
			os.write(data);
			os.flush();
			os.close();
			
			if (con.getResponseCode() < 400) {
		    is = con.getInputStream();
			} else {
				is = con.getErrorStream();
			}

			final String encoding = con.getContentEncoding();

			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				Log.i(TAG, "callPost(): GZIP OK");
				is = new GZIPInputStream(is);
			} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
				Log.i(TAG, "callPost(): DEFLATE OK");
				is = new InflaterInputStream(is, new Inflater(true));
			} else {
				Log.i(TAG, "callPost(): WITHOUT COMPRESSION");
			}

			isr = new InputStreamReader(is, "UTF-8");
			
			return new JsonReader(isr);			
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
			throw new GeocachingApiException("Error while downloading data (" + e.getClass().getSimpleName() + "): " + e.getMessage(), e);
		}
	}
	
	protected String maskPassword(String input) {
		int start;
		if ((start = input.indexOf("&Password=")) == -1)
			return input;

		return input.substring(0, start + 10) + "******" + input.substring(input.indexOf('&', start + 10));
	}

	@Override
	public void onProgressUpdate(int progress) {
		fireProgressListener(progress);
	}
}
