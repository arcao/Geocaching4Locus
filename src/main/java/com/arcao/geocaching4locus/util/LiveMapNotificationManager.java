package com.arcao.geocaching4locus.util;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.arcao.geocaching4locus.DashboardActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.receiver.LiveMapBroadcastReceiver;
import locus.api.android.ActionTools;
import locus.api.android.utils.LocusInfo;
import timber.log.Timber;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public class LiveMapNotificationManager implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String VAR_B_MAP_VISIBLE = ("1300");

	private static final String ACTION_HIDE_NOTIFICATION = "com.arcao.geocaching4locus.action.HIDE_NOTIFICATION";
	private static final String ACTION_LIVE_MAP_ENABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_ENABLE";
	private static final String ACTION_LIVE_MAP_DISABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_DISABLE";
	private static final long NOTIFICATION_TIMEOUT_MS = 1500;
	private static final int NOTIFICATION_ID = R.string.menu_live_map; // something unique

	private static boolean mNotificationShown = false;
	private static boolean mLastLiveMapState = false;

	private final Context mContext;
	private final NotificationManager mNotificationManager;
	private final SharedPreferences mSharedPrefs;
	private final boolean mShowLiveMapDisabledNotification;
	private final boolean mShowLiveMapVisibleOnlyNotification;

	private final Collection<LiveMapStateChangeListener> mStateChangeListeners = new CopyOnWriteArraySet<>();

	public static LiveMapNotificationManager get(Context context) {
		return new LiveMapNotificationManager(context);
	}

	private LiveMapNotificationManager(Context context) {
		mContext = context.getApplicationContext();

		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		mShowLiveMapDisabledNotification = mSharedPrefs.getBoolean(PrefConstants.SHOW_LIVE_MAP_DISABLED_NOTIFICATION, false);
		mShowLiveMapVisibleOnlyNotification = true;
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
				if (mShowLiveMapDisabledNotification) {
					showNotification();
				} else {
					hideNotification();
				}
				return true;
			default:
				if (!isLiveMapEnabled() && !mShowLiveMapDisabledNotification) {
					return false;
				}

				if (!isMapVisible(intent) && mShowLiveMapVisibleOnlyNotification) {
					return false;
				}

				if (!mNotificationShown || mLastLiveMapState != isLiveMapEnabled()) {
					showNotification();
					//noinspection AssignmentToStaticFieldFromInstanceMethod
					mLastLiveMapState = isLiveMapEnabled();
				}
				updateNotificationHideAlarm();
				return false;
		}
	}

	private static boolean isMapVisible(Intent intent) {
		return intent.getBooleanExtra(VAR_B_MAP_VISIBLE, false);
	}

	public boolean isForceUpdateRequiredInFuture() {
		return !mNotificationShown;
	}

	public void setDownloadingProgress(int current, int count) {
		if (!mNotificationShown)
			return;

		NotificationCompat.Builder nb = createBaseNotification();

		if (current == 0) {
			nb.setProgress(0, 0, true);
		} else if (current < count) {
			nb.setProgress(count, current, false);
		}

		if (current < count) {
			nb.setSmallIcon(R.drawable.ic_stat_location_map_downloading_anim);
			nb.setContentText(mContext.getResources().getString(R.string.livemap_notification_message_downloading, current, count, (current * 100) / count));
		}

		mNotificationManager.notify(NOTIFICATION_ID, nb.build());
	}

	private void updateNotificationHideAlarm() {
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = createPendingIntent(ACTION_HIDE_NOTIFICATION);

		alarmManager.cancel(pendingIntent);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + NOTIFICATION_TIMEOUT_MS, pendingIntent);
	}

	private void showNotification() {
		//noinspection AssignmentToStaticFieldFromInstanceMethod
		mNotificationShown = true;

		NotificationCompat.Builder builder = createBaseNotification();
		mNotificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	private void hideNotification() {
		//noinspection AssignmentToStaticFieldFromInstanceMethod
		mNotificationShown = false;

		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingIntent = createPendingIntent(ACTION_HIDE_NOTIFICATION);

		alarmManager.cancel(pendingIntent);
		mNotificationManager.cancel(NOTIFICATION_ID);
	}

	private NotificationCompat.Builder createBaseNotification() {
		NotificationCompat.Builder nb = new NotificationCompat.Builder(mContext);

		nb.setOngoing(true);
		nb.setWhen(0); // this fix redraw issue while refreshing
		nb.setLocalOnly(true);
		nb.setCategory(NotificationCompat.CATEGORY_SERVICE);

		nb.setContentTitle(mContext.getText(R.string.livemap_notification_title));

		if (isLiveMapEnabled()) {
			nb.setSmallIcon(R.drawable.ic_stat_location_map);
			nb.setContentText(mContext.getText(R.string.livemap_notification_message_enabled));
			nb.addAction(R.drawable.ic_stat_navigation_cancel, mContext.getText(R.string.livemap_notification_action_disable), createPendingIntent(ACTION_LIVE_MAP_DISABLE));
		} else {
			nb.setSmallIcon(R.drawable.ic_stat_location_map_disabled);
			nb.setContentText(mContext.getText(R.string.livemap_notification_message_disabled));
			nb.addAction(R.drawable.ic_stat_navigation_accept, mContext.getText(R.string.livemap_notification_action_enable), createPendingIntent(ACTION_LIVE_MAP_ENABLE));
		}

		nb.setPriority(NotificationCompat.PRIORITY_MAX); // always show button

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			nb.setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext, DashboardActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
		}

		return nb;
	}

	private PendingIntent createPendingIntent(String action) {
		Intent intent = new Intent(action, null, mContext, LiveMapBroadcastReceiver.class);
		return PendingIntent.getBroadcast(mContext, 0, intent, 0);
	}

	public void setLiveMapEnabled(boolean enabled) {
		boolean periodicUpdateEnabled = true;
		try {
			LocusInfo info = ActionTools.getLocusInfo(mContext, LocusTesting.getActiveVersion(mContext));
			if (info != null) {
				periodicUpdateEnabled = info.isPeriodicUpdatesEnabled();
			}
		} catch (Throwable e) {
			Timber.e(e, "Unable to receive info about current state of periodic update events from Locus.");
		}

		if (enabled && !periodicUpdateEnabled) {
			enabled = false;

			Toast.makeText(mContext, mContext.getText(R.string.livemap_disabled), Toast.LENGTH_LONG).show();
		} else if (enabled) {
			Toast.makeText(mContext, mContext.getText(R.string.livemap_activated), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, mContext.getText(R.string.livemap_deactivated), Toast.LENGTH_LONG).show();
		}

		mSharedPrefs.edit().putBoolean(PrefConstants.LIVE_MAP, enabled).apply();
	}

	public boolean isLiveMapEnabled() {
		return mSharedPrefs.getBoolean(PrefConstants.LIVE_MAP, false);
	}

	public void addLiveMapStateChangeListener(LiveMapStateChangeListener liveMapStateChangeListener) {
		mStateChangeListeners.add(liveMapStateChangeListener);

		if (mStateChangeListeners.size() == 1) {
			mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
		}
	}

	public void removeLiveMapStateChangeListener(LiveMapStateChangeListener liveMapStateChangeListener) {
		mStateChangeListeners.remove(liveMapStateChangeListener);

		if (mStateChangeListeners.size() == 0) {
			mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (PrefConstants.LIVE_MAP.equals(key)) {
			for(LiveMapStateChangeListener listener : mStateChangeListeners) {
				listener.onLiveMapStateChange(sharedPreferences.getBoolean(key, false));
			}
		}
	}

	public interface LiveMapStateChangeListener {
		void onLiveMapStateChange(boolean newState);
	}
}
