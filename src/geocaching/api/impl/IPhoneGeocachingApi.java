package geocaching.api.impl;

import geocaching.api.AbstractGeocachingApi;
import geocaching.api.data.CacheLog;
import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.TravelBug;
import geocaching.api.data.UserWaypoint;
import geocaching.api.data.Waypoint;
import geocaching.api.data.type.AttributeType;
import geocaching.api.data.type.CacheType;
import geocaching.api.data.type.ContainerType;
import geocaching.api.data.type.LogType;
import geocaching.api.data.type.WayPointType;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.exception.InvalidCredentialsException;
import geocaching.api.exception.InvalidSessionException;
import geocaching.api.impl.iphone_geocaching_api.StatusCode;
import geocaching.api.util.StringUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import android.util.Log;

public class IPhoneGeocachingApi extends AbstractGeocachingApi {
	private static final String GHOST_USERNAME = "GroundspeakAPI";
	private static final String GHOST_PASSWORD = "h3rd1ngc@ts!";

	private static final String APP_KEY = "e0dc6788-c880-4d3c-8903-3e2230650281";
	private static final String BASE_URL = "https://api.groundspeak.com/mango/services.asmx/";

	// XML Namespaces
	private static final Namespace NS_SESSION_DATA_SET = Namespace.getNamespace("http://tempuri.org/SessionDataSet2.xsd");
	private static final Namespace NS_STATUS_DATA_SET = Namespace.getNamespace("http://tempuri.org/StatusDataSet.xsd");
	private static final Namespace NS_CACHE_SIMPLE_DATA_SET = Namespace.getNamespace("http://tempuri.org/CacheSimpleDataSet.xsd");
	private static final Namespace NS_GPX = Namespace.getNamespace("http://www.topografix.com/GPX/1/1");
	private static final Namespace NS_GS = Namespace.getNamespace("http://www.groundspeak.com/cache/1/0");

	// Date formats
	private static final DateFormat[] GPX_TIME_FMT = {
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
	};
	private static final DateFormat XSD_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	private static final String TAG = "Geocaching4Locus|IPhoneGeocachingApi";

	private final SAXBuilder builder;

	public IPhoneGeocachingApi() {
		builder = new SAXBuilder();
	}

	@Override
	public void openSession(String userName, String password) throws GeocachingApiException {
		try {
			Element root = callGet(
					"OpenSessionEx?licenseKey=" + APP_KEY +
					"&userName=" + URLEncoder.encode(userName, "UTF-8") +
					"&password=" + URLEncoder.encode(password, "UTF-8") +
					"&deviceId=" + getDeviceId(userName) +
					"&language=en&version=4.0&checksum=&deviceType=iPhone&schemaName=SessionDataSet2.xsd"
			);

			checkError(root);

			Element session2 = root.getChild("Session2", NS_SESSION_DATA_SET);
			Element sessionGuid = session2.getChild("SessionGuid", NS_SESSION_DATA_SET);
			session = sessionGuid.getTextTrim();
			Log.i(TAG, "Session: " + session);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.toString(), e);
			session = null;
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
	public List<SimpleGeocache> getCachesByCoordinates(double latitude, double longitude, int startPosition, int endPosition, float radiusMiles,
			CacheType[] cacheTypes)
			throws GeocachingApiException {
		Element root;
		try {
			root = callGet(
					"GetCachesByPointWithDeviceFilter?sessionToken=" + session +
					"&schemaName=CacheSimpleDataSet.xsd" +
					"&startPos=" + startPosition +
					"&endPos=" + endPosition +
					"&cacheTypeNamesList=" + URLEncoder.encode(StringUtils.join(cacheTypes, ','), "UTF-8") +
					"&latitude=" + latitude +
					"&longitude=" + longitude +
					"&radiusMiles=" + radiusMiles +
					"&deviceIdentifier=&pinNumber="
			);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.toString(), e);
			return null;
		}

		checkError(root);

		@SuppressWarnings("unchecked")
		List<Element> list = root.getChildren("Cache", NS_CACHE_SIMPLE_DATA_SET);

		List<SimpleGeocache> caches = new ArrayList<SimpleGeocache>();

		for (Element child : list) {
			caches.add(new SimpleGeocache(
					child.getChildTextTrim("CacheCode", NS_CACHE_SIMPLE_DATA_SET),
					child.getChildTextTrim("Name", NS_CACHE_SIMPLE_DATA_SET),
					Double.parseDouble(child.getChildTextTrim("Longitude", NS_CACHE_SIMPLE_DATA_SET)),
					Double.parseDouble(child.getChildTextTrim("Latitude", NS_CACHE_SIMPLE_DATA_SET)),
					CacheType.parseCacheType(child.getChildTextTrim("CacheTypeName", NS_CACHE_SIMPLE_DATA_SET)),
					Float.parseFloat(child.getChildTextTrim("DifficultyRating", NS_CACHE_SIMPLE_DATA_SET)),
					Float.parseFloat(child.getChildTextTrim("TerrainRating", NS_CACHE_SIMPLE_DATA_SET)),
					child.getChildTextTrim("UserGUID", NS_CACHE_SIMPLE_DATA_SET),
					child.getChildTextTrim("UserName", NS_CACHE_SIMPLE_DATA_SET),
					Boolean.parseBoolean(child.getChildTextTrim("IsAvailable", NS_CACHE_SIMPLE_DATA_SET)),
					Boolean.parseBoolean(child.getChildTextTrim("IsArchived", NS_CACHE_SIMPLE_DATA_SET)),
					Boolean.parseBoolean(child.getChildTextTrim("IsPremiumListing", NS_CACHE_SIMPLE_DATA_SET)),
					child.getChildTextTrim("CountryName", NS_CACHE_SIMPLE_DATA_SET),
					child.getChildTextTrim("StateName", NS_CACHE_SIMPLE_DATA_SET),
					parseXSDDate(child.getChildTextTrim("Created", NS_CACHE_SIMPLE_DATA_SET)),
					child.getChildTextTrim("ContactName", NS_CACHE_SIMPLE_DATA_SET),
					ContainerType.parseContainerType(child.getChildTextTrim("ContainerName", NS_CACHE_SIMPLE_DATA_SET)),
					Integer.parseInt(child.getChildTextTrim("TrackableCount", NS_CACHE_SIMPLE_DATA_SET)),
					Boolean.parseBoolean(child.getChildTextTrim("HasFound", NS_CACHE_SIMPLE_DATA_SET))
			));
		}

		return caches;
	}

	@Override
	public SimpleGeocache getCacheSimple(String cacheCode) throws GeocachingApiException {
		Element root = callGet(
				"GetCachesByCacheCode?sessionToken=" + session +
				"&schemaName=CacheSimpleDataSet.xsd" +
				"&cacheCode=" + cacheCode
		);

		checkError(root);

		Element child = root.getChild("Cache", NS_CACHE_SIMPLE_DATA_SET);
		if (child == null || child.getChild("CacheCode", NS_CACHE_SIMPLE_DATA_SET) == null)
			return null;

		return new SimpleGeocache(
				child.getChildTextTrim("CacheCode", NS_CACHE_SIMPLE_DATA_SET),
				child.getChildTextTrim("Name", NS_CACHE_SIMPLE_DATA_SET),
				Double.parseDouble(child.getChildTextTrim("Longitude", NS_CACHE_SIMPLE_DATA_SET)),
				Double.parseDouble(child.getChildTextTrim("Latitude", NS_CACHE_SIMPLE_DATA_SET)),
				CacheType.parseCacheType(child.getChildTextTrim("CacheTypeName", NS_CACHE_SIMPLE_DATA_SET)),
				Float.parseFloat(child.getChildTextTrim("DifficultyRating", NS_CACHE_SIMPLE_DATA_SET)),
				Float.parseFloat(child.getChildTextTrim("TerrainRating", NS_CACHE_SIMPLE_DATA_SET)),
				child.getChildTextTrim("UserGUID", NS_CACHE_SIMPLE_DATA_SET),
				child.getChildTextTrim("UserName", NS_CACHE_SIMPLE_DATA_SET),
				Boolean.parseBoolean(child.getChildTextTrim("IsAvailable", NS_CACHE_SIMPLE_DATA_SET)),
				Boolean.parseBoolean(child.getChildTextTrim("IsArchived", NS_CACHE_SIMPLE_DATA_SET)),
				Boolean.parseBoolean(child.getChildTextTrim("IsPremiumListing", NS_CACHE_SIMPLE_DATA_SET)),
				child.getChildTextTrim("CountryName", NS_CACHE_SIMPLE_DATA_SET),
				child.getChildTextTrim("StateName", NS_CACHE_SIMPLE_DATA_SET),
				parseXSDDate(child.getChildTextTrim("Created", NS_CACHE_SIMPLE_DATA_SET)),
				child.getChildTextTrim("ContactName", NS_CACHE_SIMPLE_DATA_SET),
				ContainerType.parseContainerType(child.getChildTextTrim("ContainerName", NS_CACHE_SIMPLE_DATA_SET)),
				Integer.parseInt(child.getChildTextTrim("TrackableCount", NS_CACHE_SIMPLE_DATA_SET)),
				Boolean.parseBoolean(child.getChildTextTrim("HasFound", NS_CACHE_SIMPLE_DATA_SET))
		);
	}

	@Override
	public Geocache getCache(String cacheCode) throws GeocachingApiException {
		int i;

		Element root = callGet(
				"GetCachesByCacheCode?sessionToken=" + session +
				"&schemaName=CacheGPXDataSet.xsd" +
				"&cacheCode=" + cacheCode
		);

		checkError(root);

		@SuppressWarnings("unchecked")
		List<Element> waypointsEl = root.getChildren("wpt", NS_GPX);
		if (waypointsEl.size() == 0)
			return null;

		List<Waypoint> waypoints = new ArrayList<Waypoint>();
		i = -1;
		for (Element waypointEl : waypointsEl) {
			if (i == -1) {
				i++;
				continue;
			}

			waypoints.add(new Waypoint(
					Double.parseDouble(waypointEl.getAttributeValue("lon", "0")), // longitude
					Double.parseDouble(waypointEl.getAttributeValue("lat", "0")), // latitude
					parseGPXDate(waypointEl.getChildTextTrim("time", NS_GPX)), // time
					waypointEl.getChildTextTrim("name", NS_GPX), // waypoint geo code
					waypointEl.getChildTextTrim("desc", NS_GPX), // name
					waypointEl.getChildTextTrim("cmt", NS_GPX), // cmt
					WayPointType.parseWayPointType(waypointEl.getChildTextTrim("sym", NS_GPX))
			));
			i++;
		}

		Element cache = waypointsEl.get(0);
		Element extCache = cache.getChild("extensions", NS_GPX).getChild("cache", NS_GS);

		// parse logs
		List<CacheLog> logs = new ArrayList<CacheLog>();
		Element logsContainer = extCache.getChild("logs", NS_GS);
		if (logsContainer != null) {
			@SuppressWarnings("unchecked")
			List<Element> logsEl = logsContainer.getChildren("log", NS_GS);

			for (Element logEl : logsEl) {
				logs.add(new CacheLog(
						parseGPXDate(logEl.getChildTextTrim("date", NS_GS)), // date
						LogType.parseLogType(logEl.getChildTextTrim("type", NS_GS)), // logType
						logEl.getChildTextTrim("finder", NS_GS), // author
						logEl.getChildTextTrim("text", NS_GS) // text
				));
			}
		}

		// parse travel bugs
		List<TravelBug> travelBugs = new ArrayList<TravelBug>();
		Element trackableContainer = extCache.getChild("travelbugs", NS_GS);
		if (trackableContainer != null) {
			@SuppressWarnings("unchecked")
			List<Element> trackablesEl = trackableContainer.getChildren("travelbug", NS_GS);

			for (Element trackableEl : trackablesEl) {
				travelBugs.add(new TravelBug(
						trackableEl.getAttributeValue("ref"),
						trackableEl.getChildTextTrim("name", NS_GS),
						cache.getChildTextTrim("name", NS_GS)
				));
			}
		}

		// parse cache
		Geocache g = new Geocache(
				cache.getChildTextTrim("name", NS_GPX), // geoCode
				extCache.getChildTextTrim("name", NS_GS), // name
				Double.parseDouble(cache.getAttributeValue("lon")), // longitude
				Double.parseDouble(cache.getAttributeValue("lat")), // latitude
				CacheType.parseCacheType(extCache.getChildTextTrim("type", NS_GS)), // cacheType
				Float.parseFloat(extCache.getChildTextTrim("difficulty", NS_GS)), // difficultyRating
				Float.parseFloat(extCache.getChildTextTrim("terrain", NS_GS)), // terrainRatting
				"", // TODO authorGuid
				extCache.getChildTextTrim("placed_by", NS_GS), // authorName
				Boolean.parseBoolean(extCache.getAttributeValue("available")), // available
				Boolean.parseBoolean(extCache.getAttributeValue("archived")), // archived
				false, // TODO premium listing
				extCache.getChildTextTrim("country", NS_GS), // countryName
				extCache.getChildTextTrim("state", NS_GS), // stateName
				parseGPXDate(cache.getChildTextTrim("time", NS_GPX)), // created
				extCache.getChildTextTrim("placed_by", NS_GS), // contactName
				ContainerType.parseContainerType(extCache.getChildTextTrim("container", NS_GS)), // containerType
				travelBugs.size(), // trackableCount
				false, // TODO found
				extCache.getChildTextTrim("short_description", NS_GS), // shortDescription
				extCache.getChildTextTrim("long_description", NS_GS), // longDescription
				extCache.getChildTextTrim("encoded_hints", NS_GS), // hint
				logs, // cacheLogs
				travelBugs, // travelBugs
				waypoints, // wayPoints
				new ArrayList<AttributeType>(),
				new ArrayList<UserWaypoint>()
		);

		return g;
	}

	@Override
	public List<Waypoint> getWayPointsByCache(String cacheCode) throws GeocachingApiException {
		throw new GeocachingApiException("Not implemented.");
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

	// ----------------------- Helper methods

	private String getDeviceId(String userName) {
		try {
			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
			byte[] hash = sha1.digest(userName.getBytes());

			Formatter formatter = new Formatter();
			for (byte b : hash) {
				formatter.format("%02x", b);
			}
			return formatter.toString();
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.toString(), e);
			return "";
		}

	}

	private Date parseXSDDate(String date) {
		Date d = XSD_TIME_FMT.parse(date, new ParsePosition(0));
		if (d == null) {
			Log.e(TAG, "parseXSDDate: unparsable " + date);
			d = parseGPXDate(date);
			if (d == null)
				return new Date(0);
		}
		return d;
	}

	private Date parseGPXDate(String date) {
		for (DateFormat format : GPX_TIME_FMT) {
			Date d = format.parse(date, new ParsePosition(0));
			if (d != null)
				return d;
		}
		
		Log.e(TAG, "parseGPXDate: unparsable " + date);
		return new Date(0);
	}

	private void checkError(Element root) throws GeocachingApiException {
		if (root.getChild("Status", NS_STATUS_DATA_SET) != null) {
			Element statusElement = root.getChild("Status", NS_STATUS_DATA_SET);
			String status = statusElement.getChildTextTrim("StatusCode", NS_STATUS_DATA_SET);

			StatusCode statusCode = StatusCode.parse(status);
			String statusMessage = statusCode.getErrorMessage();
			Log.i(TAG, "checkError: " + statusMessage);

			switch (statusCode) {
				case Ok:
				case NoResults:
					return;
				case UserNotAuthorized:
				case APISessionNotFound:
				case APISessionIsClosed:
					throw new InvalidSessionException(statusMessage);
				case UserLoginFailed:
				case UserNotFound:
					throw new InvalidCredentialsException(statusMessage);
				default:
					throw new GeocachingApiException(statusMessage);
			}
		}
	}

	private Element callGet(String function) throws GeocachingApiException {
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
			con.setRequestProperty("User-Agent", "Geocaching/4.0 CFNetwork/459 Darwin/10.0.0d3");
			con.setRequestProperty("Accept", "*/*");
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
			Document doc = builder.build(isr);
			return doc.getRootElement();
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
