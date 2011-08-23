package com.arcao.geocaching4locus.service;

import geocaching.api.AbstractGeocachingApiV2;
import geocaching.api.GeocachingApiProgressListener;
import geocaching.api.data.SimpleGeocache;
import geocaching.api.data.type.CacheType;
import geocaching.api.exception.GeocachingApiException;
import geocaching.api.exception.InvalidCredentialsException;
import geocaching.api.exception.InvalidSessionException;
import geocaching.api.impl.LiveGeocachingApi;
import geocaching.api.impl.live_geocaching_api.filter.CacheFilter;
import geocaching.api.impl.live_geocaching_api.filter.GeocacheExclusionsFilter;
import geocaching.api.impl.live_geocaching_api.filter.GeocacheTypeFilter;
import geocaching.api.impl.live_geocaching_api.filter.NotFoundByUsersFilter;
import geocaching.api.impl.live_geocaching_api.filter.NotHiddenByUsersFilter;
import geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import menion.android.locus.addon.publiclib.DisplayData;
import menion.android.locus.addon.publiclib.LocusConst;
import menion.android.locus.addon.publiclib.geoData.PointsData;
import menion.android.locus.addon.publiclib.utils.DataStorage;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.MainActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.provider.DataStorageProvider;
import com.arcao.geocaching4locus.util.Account;

public class SearchGeocacheService extends IntentService implements GeocachingApiProgressListener {
	private static final String TAG = "SearchGeocacheService";
	
	public static final String ACTION_PROGRESS_UPDATE = "com.arcao.geocaching4locus.intent.action.PROGRESS_UPDATE";
	public static final String ACTION_PROGRESS_COMPLETE = "com.arcao.geocaching4locus.intent.action.PROGRESS_COMPLETE";
	public static final String ACTION_ERROR = "com.arcao.geocaching4locus.intent.action.ERROR";

	public static final String PARAM_COUNT = "COUNT";
	public static final String PARAM_CURRENT = "CURRENT";
	public static final String PARAM_RESOURCE_ID = "RESOURCE_ID";
	public static final String PARAM_ADDITIONAL_MESSAGE = "ADDITIONAL_MESSAGE";
	
	public static final String PARAM_LATITUDE = "LATITUDE";
	public static final String PARAM_LONGITUDE = "LONGITUDE";
	
	private final static int MAX_PER_PAGE = 10;
	
	private boolean showFound;
	private boolean showOwn;
	private boolean simpleCacheData;
	private double distance;
	private int count = 0;
	private int current = 0;
	private Account account;
	
	private boolean canceled;
	
	protected SharedPreferences prefs;
	
	private static SearchGeocacheService instance = null;
	
	protected NotificationManager notificationManager;
	protected RemoteViews contentViews;
	protected Notification progressNotification;

	private Method startForegroundMethod;
	private Method setForegroundMethod;
	private Method stopForegroundMethod;
	
	public SearchGeocacheService() {
		super(TAG);		
	}
	
	public static SearchGeocacheService getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		prepareCompatMethods();
		
		canceled = false;

		progressNotification = createProgressNotification(); 
		startForegroundCompat(R.layout.notification_download, progressNotification);
	}

	protected Notification createProgressNotification() {
		Notification n = new Notification();
		
		n.icon = R.drawable.ic_launcher;
		n.tickerText = null;
		n.flags |= Notification.FLAG_ONGOING_EVENT;				
		n.contentView = new RemoteViews(getPackageName(), R.layout.notification_download);
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		n.contentIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, 0);
		
		return n;
	}
	
	protected Notification createErrorNotification(int resErrorId, String errorText) {
		Notification n = new Notification();
		
		n.icon = R.drawable.ic_launcher;
		n.tickerText = getText(R.string.error_title);
		n.when = new Date().getTime(); 
		
		Intent intent = new Intent(this, ErrorActivity.class);
		intent.setAction(ACTION_ERROR);
		intent.putExtra(PARAM_RESOURCE_ID, resErrorId);
		intent.putExtra(PARAM_ADDITIONAL_MESSAGE, errorText);
		
		n.setLatestEventInfo(this, getText(R.string.error_title), Html.fromHtml(getString(resErrorId, errorText)), PendingIntent.getActivity(getBaseContext(), 0, intent, 0));
		
		return n;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		loadConfiguration();
		sendProgressUpdate();
			
		double latitude = intent.getDoubleExtra(PARAM_LATITUDE, 0D);
		double longitude = intent.getDoubleExtra(PARAM_LONGITUDE, 0D);
		
		try {
			List<SimpleGeocache> caches = downloadCaches(latitude, longitude);
			sendProgressComplete();
			if (caches != null)
				callLocus(caches);
		} catch (InvalidCredentialsException e) {
			sendError(R.string.error_credentials, null);
		} catch (Exception e) {
			String message = e.getMessage();
			if (message == null)
				message = "";
			
			sendError(R.string.error, String.format("<br>%s<br> <br>Exception: %s<br>File: %s<br>Line: %d", message, e.getClass().getSimpleName(), e.getStackTrace()[0].getFileName(), e.getStackTrace()[0].getLineNumber()));
		}

	}
	
	@Override
	public void onDestroy() {
		canceled = true;
		instance = null;
		stopForegroundCompat(R.layout.notification_download);
		super.onDestroy();
	}

	protected void loadConfiguration() {
		showFound = prefs.getBoolean("filter_show_found", false);
		showOwn = prefs.getBoolean("filter_show_own", false);
		simpleCacheData = prefs.getBoolean("simple_cache_data", false);
		
		distance = prefs.getFloat("distance", 160.9344F);
		if (!prefs.getBoolean("imperial_units", false)) {
			distance = distance * 1.609344;
		}

		current = 0;
		count = prefs.getInt("filter_count_of_caches", 20);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("username", "");
		String password = prefs.getString("password", "");
		String session = prefs.getString("session", null);

		account = new Account(userName, password, session);
	}

	private void callLocus(List<SimpleGeocache> caches) {
		boolean importCaches = prefs.getBoolean("import_caches", false);
		
		try {
			ArrayList<PointsData> pointDataCollection = new ArrayList<PointsData>();
			
			// beware there is row limit in DataStorageProvider (1MB per row - serialized PointsData is one row)
			// so we split points into several PointsData object with same uniqueName - Locus merge it automatically
			// since version 1.9.5
			PointsData points = new PointsData("Geocaching");
			for (SimpleGeocache cache : caches) {				
				if (points.getPoints().size() >= 50) {
					pointDataCollection.add(points);
					points = new PointsData("Geocaching");
				}
				// convert SimpleGeocache to Point
				points.addPoint(cache.toPoint());
			}
			
			if (!points.getPoints().isEmpty())
				pointDataCollection.add(points);
			
			// set data
			DataStorage.setData(pointDataCollection);
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(LocusConst.EXTRA_POINTS_CURSOR_URI, DataStorageProvider.URI);
			DisplayData.sendData(getApplication(), intent, importCaches);
		} catch (Exception e) {
			Log.e(TAG, "callLocus()", e);
		}
	}
	
	protected CacheType[] getCacheTypeFilterResult() {
		Vector<CacheType> filter = new Vector<CacheType>();

		for (int i = 0; i < CacheType.values().length; i++) {
			if (prefs.getBoolean("filter_" + i, true)) {
				filter.add(CacheType.values()[i]);
			}
		}

		return filter.toArray(new CacheType[0]);
	}
	
	protected List<SimpleGeocache> downloadCaches(double latitude, double longitude) throws GeocachingApiException {
		final List<SimpleGeocache> caches = new ArrayList<SimpleGeocache>();
					
		if (account.getUserName() == null || account.getUserName().length() == 0 || account.getPassword() == null || account.getPassword().length() == 0)
			throw new InvalidCredentialsException("Username or password is empty.");

		if (canceled)
			return null;
		
		AbstractGeocachingApiV2 api = new LiveGeocachingApi();
		login(api, account);
		
		sendProgressUpdate();
		api.addProgressListener(this);
		try {
			current = 0;
			while (current < count) {
				int perPage = (count - current < MAX_PER_PAGE) ? count - current : MAX_PER_PAGE;
				
				List<SimpleGeocache> cachesToAdd = api.searchForGeocachesJSON(simpleCacheData, current, perPage, -1, -1, new CacheFilter[] {
						new PointRadiusFilter(latitude, longitude, (long) (distance * 1000)),
						new GeocacheTypeFilter(getCacheTypeFilterResult()),
						new GeocacheExclusionsFilter(false, true, null),
						new NotFoundByUsersFilter(showFound ? null : account.getUserName()),
						new NotHiddenByUsersFilter(showOwn ? null : account.getUserName())
				});
				
				if (canceled)
					return null;
				
				if (cachesToAdd.size() == 0)
					break;
								
				caches.addAll(cachesToAdd);
				
				current = current + perPage;
				
				sendProgressUpdate();
			}
			int count = caches.size();
			
			Log.i(TAG, "found caches: " + count);

			return caches;
		} catch (InvalidSessionException e) {
			account.setSession(null);
			
			Editor edit = prefs.edit();
			edit.remove("session");
			edit.commit();
			
			return downloadCaches(latitude, longitude);
		} finally {
			api.removeProgressListener(this);
			account.setSession(api.getSession());
			if (account.getSession() != null && account.getSession().length() > 0) {
				Editor edit = prefs.edit();
				edit.putString("session", account.getSession());
				edit.commit();
			}
		}
	}

	private void login(AbstractGeocachingApiV2 api, Account account) throws GeocachingApiException, InvalidCredentialsException {
		try {
			if (account.getSession() == null || account.getSession().length() == 0) {
				api.openSession(account.getUserName(), account.getPassword());
			} else {
				api.openSession(account.getSession());
			}
		} catch (InvalidCredentialsException e) {
			Log.e(TAG, "Creditials not valid.", e);
			throw e;
		}
	}
	
	@Override
	public void onProgressUpdate(int progress) {
		if (canceled)
			return;
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_UPDATE);
		//broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_COUNT, count + progress);
		broadcastIntent.putExtra(PARAM_CURRENT, current);
		sendBroadcast(broadcastIntent);
	}
	
	public void sendProgressUpdate() {
		if (canceled)
			return;
		
		progressNotification.contentView.setProgressBar(R.id.progress_bar, count, current, false);
		progressNotification.contentView.setTextViewText(R.id.progress_text, ((current * 100) / count) + "%");
		notificationManager.notify(R.layout.notification_download, progressNotification);
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_UPDATE);
		//broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_COUNT, count);
		broadcastIntent.putExtra(PARAM_CURRENT, current);
		sendBroadcast(broadcastIntent);
	}
	
	protected void sendProgressComplete() {
		if (!canceled) {
			progressNotification.contentView.setProgressBar(R.id.progress_bar, count, current, false);
			progressNotification.contentView.setTextViewText(R.id.progress_text, "100%");
			notificationManager.notify(R.layout.notification_download, progressNotification);
		}
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_COMPLETE);
		//broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_COUNT, count);
		broadcastIntent.putExtra(PARAM_CURRENT, count);
		sendBroadcast(broadcastIntent);
	}
	
	protected void sendError(int error, String additionalMessage) {
		// error notification
		notificationManager.notify(error, createErrorNotification(error, additionalMessage));
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_ERROR);
		//broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PARAM_RESOURCE_ID, error);
		if (additionalMessage != null)
			broadcastIntent.putExtra(PARAM_ADDITIONAL_MESSAGE, additionalMessage);
		sendBroadcast(broadcastIntent);
	}

	// ----------------  Compatible methods for start / stop foreground service --------------------
	
	protected void prepareCompatMethods() {
		try {
			startForegroundMethod = getClass().getMethod("startForeground", new Class[] {int.class, Notification.class});
			stopForegroundMethod = getClass().getMethod("stopForeground", new Class[] {boolean.class});
			return;
		} catch (NoSuchMethodException e) {
			// Running on an older platform.
			startForegroundMethod = stopForegroundMethod = null;
		}
		try {
			setForegroundMethod = getClass().getMethod("setForeground", new Class[] {boolean.class});
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
		}
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	protected void startForegroundCompat(int id, Notification notification) {
	    // If we have the new startForeground API, then use it.
	    if (startForegroundMethod != null) {
	        invokeMethod(startForegroundMethod, new Object[] { id, notification});
	        return;
	    }

	    // Fall back on the old API.
	    invokeMethod(setForegroundMethod, new Object[] {true});
	    notificationManager.notify(id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	protected void stopForegroundCompat(int id) {
	    // If we have the new stopForeground API, then use it.
	    if (stopForegroundMethod != null) {
	        invokeMethod(stopForegroundMethod, new Object[] { true });
	        return;
	    }

	    // Fall back on the old API.  Note to cancel BEFORE changing the
	    // foreground state, since we could be killed at that point.
	    notificationManager.cancel(id);
	    invokeMethod(setForegroundMethod, new Object[] { false });
	}
	
	protected void invokeMethod(Method method, Object[] args) {
    try {
        method.invoke(this, args);
    } catch (InvocationTargetException e) {
        // Should not happen.
        Log.w(TAG, "Unable to invoke method", e);
    } catch (IllegalAccessException e) {
        // Should not happen.
        Log.w(TAG, "Unable to invoke method", e);
    }
	}
}
