package com.arcao.geocaching4locus.util;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.receiver.LiveMapBroadcastReceiver;
import locus.api.android.ActionTools;

public class LiveMapNotificationManager {
	protected static final String TAG = "LiveMapNM";
	protected static final String ACTION_HIDE_NOTIFICATION = "com.arcao.geocaching4locus.action.HIDE_NOTIFICATION";
	protected static final String ACTION_LIVE_MAP_ENABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_ENABLE";
	protected static final String ACTION_LIVE_MAP_DISABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_DISABLE";
	protected static final long NOTIFICATION_TIMEOUT_MS = 5000;
	protected static final int NOTIFICATION_ID = R.string.menu_live_map; // something unique

	protected static boolean notificationShown = false;
	protected static boolean lastLiveMapState = false;

	protected Context mContext;
	protected NotificationManager mNotificationManager;
	protected SharedPreferences mSharedPrefs;
	protected boolean showLiveMapDisabledNotification;

	public static LiveMapNotificationManager get(Context context) {
		return new LiveMapNotificationManager(context);
	}

	private LiveMapNotificationManager(Context context) {
		mContext = context;

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		showLiveMapDisabledNotification = mSharedPrefs.getBoolean(PrefConstants.SHOW_LIVE_MAP_DISABLED_NOTIFICATION, false);
	}

	public boolean handleBroadcastIntent(Intent intent) {
		if (intent == null)
			return false;

		switch(intent.getAction()) {
			case ACTION_HIDE_NOTIFICATION:
				hideNotification();
				return true;
			case ACTION_LIVE_MAP_ENABLE:
				setLiveMapEnabled(true);
				showNotification();
				return true;
			case ACTION_LIVE_MAP_DISABLE:
				setLiveMapEnabled(false);
				if (showLiveMapDisabledNotification) {
					showNotification();
				} else {
					hideNotification();
				}
				return true;
			default:
				if (!isLiveMapEnabled() && !showLiveMapDisabledNotification) {
					return false;
				}

				if (!notificationShown || lastLiveMapState != isLiveMapEnabled()) {
					showNotification();
					lastLiveMapState = isLiveMapEnabled();
				}
				setNotificationHideAlarm();
				return false;
		}
	}

	public void setDownloadingProgress(int progress) {
		if (!notificationShown)
			return;

		NotificationCompat.Builder nb = createBaseNotification();

		if (progress == 0) {
			nb.setProgress(0, 0, true);
		} else if (progress < 100) {
			nb.setProgress(100, progress, false);
		}

		if (progress < 100) {
			nb.setContentText(mContext.getText(R.string.livemap_notification_message_downloading));
		}

		mNotificationManager.notify(NOTIFICATION_ID, nb.build());
	}

	protected void setNotificationHideAlarm() {
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = createPendingIntent(ACTION_HIDE_NOTIFICATION);

		alarmManager.cancel(pendingIntent);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + NOTIFICATION_TIMEOUT_MS, pendingIntent);
	}

	protected void showNotification() {
		notificationShown = true;

		NotificationCompat.Builder builder = createBaseNotification();
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	protected void hideNotification() {
		notificationShown = false;

		// disable live map
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		prefs.edit().putBoolean(PrefConstants.LIVE_MAP, false).commit();

		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = createPendingIntent(ACTION_HIDE_NOTIFICATION);

		alarmManager.cancel(pendingIntent);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	protected NotificationCompat.Builder createBaseNotification() {
		NotificationCompat.Builder nb = new NotificationCompat.Builder(mContext);

		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setOngoing(true);
		nb.setWhen(0); // this fix redraw issue while refreshing

		nb.setContentTitle(mContext.getText(R.string.livemap_notification_title));

		if (isLiveMapEnabled()) {
			nb.setContentText(mContext.getText(R.string.livemap_notification_message_enabled));
			nb.addAction(R.drawable.ic_stat_navigation_cancel, mContext.getText(R.string.livemap_notification_action_disable), createPendingIntent(ACTION_LIVE_MAP_DISABLE));
		} else {
			nb.setContentText(mContext.getText(R.string.livemap_notification_message_disabled));
			nb.addAction(R.drawable.ic_stat_navigation_accept, mContext.getText(R.string.livemap_notification_action_enable), createPendingIntent(ACTION_LIVE_MAP_ENABLE));
		}

		nb.setPriority(NotificationCompat.PRIORITY_MAX); // always show button

		return nb;
	}

	protected PendingIntent createPendingIntent(String action) {
		Intent intent = new Intent(action, null, mContext, LiveMapBroadcastReceiver.class);
		return PendingIntent.getBroadcast(mContext, 0, intent, 0);
	}

	public void setLiveMapEnabled(boolean enabled) {
		boolean periodicUpdateEnabled = true;
		try {
			periodicUpdateEnabled = ActionTools.isPeriodicUpdatesEnabled(mContext);;
		} catch (Exception e) {
			Log.e(TAG, "Unable to receive info about current state of periodic update events from Locus.", e);
		}

		if (enabled && !periodicUpdateEnabled) {
			enabled = false;

			Toast.makeText(mContext, mContext.getText(R.string.livemap_disabled), Toast.LENGTH_LONG).show();
		} else if (enabled) {
			Toast.makeText(mContext, mContext.getText(R.string.livemap_activated), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, mContext.getText(R.string.livemap_deactivated), Toast.LENGTH_LONG).show();
		}

		mSharedPrefs.edit().putBoolean(PrefConstants.LIVE_MAP, enabled).commit();
	}

	public boolean isLiveMapEnabled() {
		return mSharedPrefs.getBoolean(PrefConstants.LIVE_MAP, false);
	}
}
