package geocaching.api.impl;

import geocaching.api.AbstractGeocachingApiV2;
import geocaching.api.data.CacheLog;
import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.TravelBug;
import geocaching.api.data.WayPoint;
import geocaching.api.data.type.CacheType;
import geocaching.api.data.type.ContainerType;
import geocaching.api.data.type.LogType;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.exception.InvalidCredentialsException;
import geocaching.api.exception.InvalidSessionException;
import geocaching.api.impl.live_geocaching_api.StatusCode;
import geocaching.api.impl.live_geocaching_api.filter.CacheFilter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class LiveGeocachingApi extends AbstractGeocachingApiV2 {
	private static final String TAG = "Geocaching4Locus|LiveGeocachingApi";
	
	private static final String GHOST_USERNAME = "";
	private static final String GHOST_PASSWORD = "";

	private static final String CONSUMER_KEY = "90C7F340-7998-477D-B4D3-AC48A9A0F560";
	private static final String LICENCE_KEY = "40940392-0C8E-487B-BC40-EA250D6D9AE0";
	
	//private static final String BASE_URL = "https://api.groundspeak.com/LiveV2/geocaching.svc/";
	private static final String BASE_URL = "https://staging.api.groundspeak.com/GreenesGang/Geocaching.svc/";
	
	private static final int BUFFER_LEN = 8192;
	
	@Override
	public void openSession(String userName, String password) throws GeocachingApiException {
		try {
			JSONObject o = callGet(
					"GetUserCredentials?ConsumerKey=" + CONSUMER_KEY +
					"&LicenseKey=" + LICENCE_KEY + 
					"&Username=" + URLEncoder.encode(userName, "UTF-8") +
					"&Password=" + URLEncoder.encode(password, "UTF-8") +
					"&format=json"
			);

			checkError(o);
			
			session = o.getString("UserGuid");
			Log.i(TAG, "Session: " + session);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.toString(), e);
			session = null;
		} catch (JSONException e) {
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
	public List<SimpleGeocache> searchForGeocachesJSON(boolean isLite, int startIndex, int maxPerPage, int geocacheLogCount, int trackableLogCount,
			CacheFilter[] filters) throws GeocachingApiException {
		
		try {
			JSONObject r =  new JSONObject();
			r.put("AccessToken", session);
			r.put("IsLite", isLite);
			r.put("StartIndex", startIndex);
			r.put("MaxPerPage", maxPerPage);
			
			if (geocacheLogCount >= 0)
				r.put("GeocacheLogCount", geocacheLogCount);
			
			if (trackableLogCount >= 0)
				r.put("TrackableLogCount", trackableLogCount);
			
			for (CacheFilter filter : filters) {
				JSONObject jsonFilter = filter.toJson();
				if (jsonFilter == null)
					continue;
				
				r.put(filter.getName(), jsonFilter);
			}
			
			JSONObject o = callPost("SearchForGeocachesJSON?format=json", r);
			checkError(o);
			
			List<SimpleGeocache> list =  new ArrayList<SimpleGeocache>();
			
			JSONArray oArray = o.getJSONArray("Geocaches");
			for (int i = 0; i < oArray.length(); i++) {
				if (isLite){
					list.add(parseSimpleGeocache(oArray.getJSONObject(i)));
				} else {
					list.add(parseGeocache(oArray.getJSONObject(i)));
				}
			}

			return list;
		} catch (JSONException e) {
			Log.e(TAG, e.toString(), e);
			throw new GeocachingApiException("Response is not valid JSON string: " + e.getMessage());
		}
	}

	protected SimpleGeocache parseSimpleGeocache(JSONObject o) throws JSONException {
		return new SimpleGeocache(
				o.getString("Code"),
				o.getString("Name"),
				o.optDouble("Longitude"), 
				o.optDouble("Latitude"), 
				CacheType.parseCacheTypeByGroundSpeakId(o.getJSONObject("CacheType").getInt("GeocacheTypeId")),
				(float) o.getDouble("Difficulty"),
				(float) o.getDouble("Terrain"),
				o.getJSONObject("Owner").getString("PublicGuid"), // user guid
				o.getString("PlacedBy"),
				o.getBoolean("Available"),
				o.getBoolean("Archived"),
				o.optBoolean("isPremium"),
				o.getString("Country"),
				o.getString("State"),
				parseJsonDate(o.getString("UTCPlaceDate")),
				o.getString("PlacedBy"), // contact name
				ContainerType.parseContainerTypeByGroundSpeakId(o.getJSONObject("ContainerType").getInt("ContainerTypeId")), //container type
				o.getInt("TrackableCount"), 
				o.getBoolean("HasbeenFoundbyUser")); // found
	}
	
	protected Geocache parseGeocache(JSONObject o) throws JSONException {
		List<CacheLog> cacheLogs = new ArrayList<CacheLog>();
		List<TravelBug> travelBugs = new ArrayList<TravelBug>();
		List<WayPoint> wayPoints = new ArrayList<WayPoint>();
		
		if (o.has("GeocacheLogs")) {
			JSONArray jsonLogs = o.getJSONArray("GeocacheLogs"); 
			for (int i = 0; i < jsonLogs.length(); i++) {
				JSONObject jsonLog = jsonLogs.getJSONObject(i);
				
				cacheLogs.add(new CacheLog(
						parseJsonDate(jsonLog.getString("UTCCreateDate")),
						LogType.parseLogType(jsonLog.getJSONObject("LogType").getString("WptLogTypeName")),
						jsonLog.getJSONObject("Finder").getString("UserName"),
						jsonLog.optString("LogText")
				));
			}
		}
		
		if (o.has("Trackables")) {
			JSONArray jsonBugs = o.getJSONArray("Trackables");
		}
		
		if (o.has("AdditionalWaypoints")) {
			JSONArray jsonWayPoints = o.getJSONArray("AdditionalWaypoints");
		}
		
		return new Geocache(
				o.getString("Code"),
				o.getString("Name"),
				o.getDouble("Longitude"), 
				o.getDouble("Latitude"), 
				CacheType.parseCacheTypeByGroundSpeakId(o.getJSONObject("CacheType").getInt("GeocacheTypeId")),
				(float) o.getDouble("Difficulty"),
				(float) o.getDouble("Terrain"),
				o.getJSONObject("Owner").getString("PublicGuid"), // user guid
				o.getString("PlacedBy"),
				o.getBoolean("Available"),
				o.getBoolean("Archived"),
				o.optBoolean("isPremium"),
				o.getString("Country"),
				o.getString("State"),
				parseJsonDate(o.getString("UTCPlaceDate")),
				o.getString("PlacedBy"), // contact name
				ContainerType.parseContainerTypeByGroundSpeakId(o.getJSONObject("ContainerType").getInt("ContainerTypeId")), //container type
				o.getInt("TrackableCount"), 
				o.getBoolean("HasbeenFoundbyUser"), 
				o.optString("ShortDescription"), 
				o.optString("LongDescription"), 
				o.optString("EncodedHints"), 
				cacheLogs, 
				travelBugs, 
				wayPoints
		);
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
	
	// -------------------- Helper methods ----------------------------------------
	
	protected void checkError(JSONObject o) throws GeocachingApiException, JSONException {
		if (o.has("Status")) {
			JSONObject statusElement = o.getJSONObject("Status");
			int code = statusElement.getInt("StatusCode");
			String statusMessage = statusElement.optString("StatusMessage");
			String exceptionDetails = statusElement.optString("ExceptionDetails");

			StatusCode statusCode = StatusCode.parseStatusCode(code);
			Log.i(TAG, "checkError: " + statusMessage);
			
			if (exceptionDetails != null && exceptionDetails.length() > 0)
				Log.e(TAG, "checkErrorDetails: " + exceptionDetails);

			switch (statusCode) {
				case OK:
					return;
				case UserAccountProblem:
				case UserDidNotAuthorize:
				case UserTokenNotValid:
					throw new InvalidSessionException(statusMessage);
				case AccountNotFound:
					throw new InvalidCredentialsException(statusMessage);
				default:
					throw new GeocachingApiException(statusMessage);
			}
		}
	}
	
	protected Date parseJsonDate(String date) {
		Pattern DATE_PATTERN = Pattern.compile("/Date\\((.*)([-+].{4})\\)/");
		
		Matcher localMatcher = DATE_PATTERN.matcher(date);
    if (localMatcher.matches())
    {
      long time = Long.parseLong(localMatcher.group(1));
      long zone = Integer.parseInt(localMatcher.group(2)) / 100 * 1000 * 60 * 60;
      return new Date(time + zone);
    }
    
    Log.e(TAG, "parseJsonDate failed: " + date);
    return new Date(0);
	}
	
	protected JSONObject callGet(String function) throws GeocachingApiException {
		InputStream is = null;
		InputStreamReader isr = null;

		Log.i(TAG, "Getting " + maskPassword(function));

		try {
			URL url = new URL(BASE_URL + function);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			// important! sometimes GC API takes too long to get response
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);

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
			StringBuffer sb = new StringBuffer();
			
			char[] buffer = new char[BUFFER_LEN];
			
			int size = 0;
			while ((size = isr.read(buffer)) != -1) {
				sb.append(buffer, 0, size);
			}
			
			return new JSONObject(sb.toString());
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
			throw new GeocachingApiException("Error while downloading data (" + e.getClass().getSimpleName() + ")", e);
		} finally {
			try { if (isr != null) isr.close(); } catch (Exception e) {}
			try { if (is != null) is.close(); } catch (Exception e) {}
		}
	}

	protected JSONObject callPost(String function, JSONObject postBody) throws GeocachingApiException {
		InputStream is = null;
		InputStreamReader isr = null;

		Log.i(TAG, "Getting " + maskPassword(function));
		
		String body = postBody.toString();

		try {
			URL url = new URL(BASE_URL + function);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();

			con.setDoOutput(true);
			
			// important! sometimes GC API takes too long to get response
			con.setConnectTimeout(10000);
			con.setReadTimeout(10000);

			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("Content-Length", Integer.toString(body.length()));
			//con.setRequestProperty("User-Agent", "Geocaching/4.0 CFNetwork/459 Darwin/10.0.0d3");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Accept-Language", "en-US");
			con.setRequestProperty("Accept-Encoding", "gzip, deflate");

			OutputStream os = con.getOutputStream();
			OutputStreamWriter osr = new OutputStreamWriter(os, "UTF-8");
			
			Log.i(TAG, "Post body: " + postBody.toString(4));
			osr.write(body);
			osr.flush();
			osr.close();
			
			if (con.getResponseCode() < 400) {
		    is = con.getInputStream();
			} else {
				is = con.getErrorStream();
			}

			final String encoding = con.getContentEncoding();

			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				Log.i(TAG, "callGet(): GZIP OK");
				is = new GZIPInputStream(is);
			} else if (encoding != null && encoding.equalsIgnoreCase("deflate")) {
				Log.i(TAG, "callGet(): DEFLATE OK");
				is = new InflaterInputStream(is, new Inflater(true));
			} else {
				Log.i(TAG, "callGet(): WITHOUT COMPRESSION");
			}

			isr = new InputStreamReader(is, "UTF-8");
			StringBuffer sb = new StringBuffer();
			
			char[] buffer = new char[BUFFER_LEN];
			
			int size = 0;
			
			while ((size = isr.read(buffer)) != -1) {
				sb.append(buffer, 0, size);
			}
			
			Log.i(TAG, "response: " + sb.toString());
			
			return new JSONObject(sb.toString());			
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
			throw new GeocachingApiException("Error while downloading data (" + e.getClass().getSimpleName() + ")", e);
		} finally {
			try { if (isr != null) isr.close(); } catch (Exception e) {}
			try { if (is != null) is.close(); } catch (Exception e) {}
		}
	}
	
	protected String maskPassword(String input) {
		int start;
		if ((start = input.indexOf("&password=")) == -1)
			return input;

		return input.substring(0, start + 10) + "******" + input.substring(input.indexOf('&', start + 10));
	}

}
