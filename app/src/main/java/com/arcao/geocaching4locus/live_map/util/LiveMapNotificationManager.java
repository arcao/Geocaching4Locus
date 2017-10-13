package com.arcao.geocaching4locus.live_map.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.util.LocusTesting;
import com.arcao.geocaching4locus.base.util.ResourcesUtil;
import com.arcao.geocaching4locus.dashboard.DashboardActivity;
import com.arcao.geocaching4locus.error.ErrorActivity;
import com.arcao.geocaching4locus.live_map.LiveMapService;
import com.arcao.geocaching4locus.live_map.receiver.LiveMapBroadcastReceiver;
import com.arcao.geocaching4locus.live_map.task.LiveMapDownloadTask;
import com.arcao.geocaching4locus.settings.SettingsActivity;
import com.arcao.geocaching4locus.settings.fragment.LiveMapPreferenceFragment;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import locus.api.android.ActionTools;
import locus.api.android.utils.LocusInfo;
import locus.api.android.utils.LocusUtils;
import timber.log.Timber;

public class LiveMapNotificationManager implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String VAR_B_MAP_VISIBLE = ("1300");

	private static final String ACTION_HIDE_NOTIFICATION = "com.arcao.geocaching4locus.action.HIDE_NOTIFICATION";
	private static final String ACTION_LIVE_MAP_ENABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_ENABLE";
	private static final String ACTION_LIVE_MAP_DISABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_DISABLE";
	private static final long NOTIFICATION_TIMEOUT_MS = 2000;
	private static final String NOTIFICATION_CHANNEL_ID = "LIVE_MAP_NOTIFICATION_CHANNEL";

	private static boolean mNotificationShown;
	private static boolean mLastLiveMapState;

	private final Context mContext;
	private final AlarmManager alarmManager;
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
		alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

		mShowLiveMapDisabledNotification = mSharedPrefs.getBoolean(PrefConstants.SHOW_LIVE_MAP_DISABLED_NOTIFICATION, false);
		mShowLiveMapVisibleOnlyNotification = true;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createChannel();
		}
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private void createChannel() {
		NotificationChannel notificationChannel = new NotificationChannel(
				NOTIFICATION_CHANNEL_ID,
				mContext.getText(R.string.menu_live_map),
				NotificationManager.IMPORTANCE_DEFAULT
		);

		mNotificationManager.createNotificationChannel(notificationChannel);
	}

	public boolean handleBroadcastIntent(Intent intent) {
		if (intent == null || intent.getAction() == null)
			return false;

		Timber.i(intent.getAction());

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

		NotificationCompat.Builder nb = createNotification();

		if (current == 0) {
			nb.setProgress(0, 0, true);
		} else if (current < count) {
			nb.setProgress(count, current, false);
		}

		if (current < count) {
			nb.setSmallIcon(R.drawable.ic_stat_location_map_downloading_anim);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
				nb.setContentText(ResourcesUtil.getText(mContext, R.string.notify_live_map_message_downloading, current, count, (current * 100) / count));
			} else {
				nb.setContentTitle(ResourcesUtil.getText(mContext, R.string.notify_live_map_message_downloading, current, count, (current * 100) / count));
			}
		}

		mNotificationManager.notify(AppConstants.NOTIFICATION_ID_LIVEMAP, nb.build());
	}

	private void updateNotificationHideAlarm() {
		PendingIntent pendingIntent = createPendingIntent(ACTION_HIDE_NOTIFICATION);

		alarmManager.cancel(pendingIntent);
		alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + NOTIFICATION_TIMEOUT_MS, pendingIntent);
	}

	private void showNotification() {
		mNotificationShown = true;

		NotificationCompat.Builder builder = createNotification();
		mNotificationManager.notify(AppConstants.NOTIFICATION_ID_LIVEMAP, builder.build());
	}

	private void hideNotification() {
		mNotificationShown = false;

		alarmManager.cancel(createPendingIntent(ACTION_HIDE_NOTIFICATION));
		LiveMapService.stop(mContext);
		mNotificationManager.cancel(AppConstants.NOTIFICATION_ID_LIVEMAP);
	}

	public NotificationCompat.Builder createNotification() {
		NotificationCompat.Builder nb = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);

		nb.setOngoing(true);
		nb.setWhen(0); // this fix redraw issue while refreshing
		nb.setLocalOnly(true);
		nb.setCategory(NotificationCompat.CATEGORY_SERVICE);

		CharSequence state;
		if (isLiveMapEnabled()) {
			nb.setSmallIcon(R.drawable.ic_stat_location_map);
			state = mContext.getText(R.string.notify_live_map_message_enabled);
			nb.addAction(R.drawable.ic_stat_navigation_cancel, mContext.getText(R.string.notify_live_map_action_disable), createPendingIntent(ACTION_LIVE_MAP_DISABLE));
		} else {
			nb.setSmallIcon(R.drawable.ic_stat_location_map_disabled);
			state = mContext.getText(R.string.notify_live_map_message_disabled);
			nb.addAction(R.drawable.ic_stat_navigation_accept, mContext.getText(R.string.notify_live_map_action_enable), createPendingIntent(ACTION_LIVE_MAP_ENABLE));
		}
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				SettingsActivity.createIntent(mContext, LiveMapPreferenceFragment.class),
				PendingIntent.FLAG_UPDATE_CURRENT);
		nb.addAction(R.drawable.ic_stat_livemap_settings, mContext.getText(R.string.notify_live_map_action_settings), pendingIntent);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
			nb.setContentTitle(mContext.getText(R.string.notify_live_map));
			nb.setContentText(state);
		} else {
			nb.setSubText(mContext.getText(R.string.menu_live_map));
			nb.setContentTitle(state);

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
		LocusUtils.LocusVersion locusVersion = LocusTesting.getActiveVersion(mContext);

		try {
			LocusInfo info = ActionTools.getLocusInfo(mContext, locusVersion);
			if (info != null) {
				periodicUpdateEnabled = info.isPeriodicUpdatesEnabled();
			}
		} catch (Throwable e) {
			Timber.e(e, "Unable to receive info about current state of periodic update events from Locus.");
		}

		// hide visible geocaches when live map is disabling
		if (!enabled && isLiveMapEnabled() && mSharedPrefs.getBoolean(PrefConstants.LIVE_MAP_HIDE_CACHES_ON_DISABLED, false)) {
			LiveMapDownloadTask.cleanMapItems(mContext);
		}

		if (enabled && !periodicUpdateEnabled) {
			enabled = false;

			showError(R.string.error_live_map_periodic_updates);
		} else if (enabled) {
			Toast.makeText(mContext, mContext.getText(R.string.toast_live_map_enabled), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mContext, mContext.getText(R.string.toast_live_map_disabled), Toast.LENGTH_LONG).show();
		}

		try {
			// make sure Live Map broadcast receiver is always enabled
			ActionTools.enablePeriodicUpdatesReceiver(mContext, locusVersion, LiveMapBroadcastReceiver.class);
		} catch (Throwable e) {
			Timber.e(e, "Unable to enable LiveMapBroadcastReceiver.");
		}

		mSharedPrefs.edit().putBoolean(PrefConstants.LIVE_MAP, enabled).apply();
	}

	private void showError(@StringRes int message) {
		mContext.startActivity(new ErrorActivity.IntentBuilder(mContext).message(message).build().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	public void showLiveMapError(@StringRes int message) {
		showLiveMapError(mContext.getText(message));
	}

	public void showLiveMapError(final CharSequence message) {
		new Handler(Looper.getMainLooper()).post(() ->
				Toast.makeText(mContext, ResourcesUtil.getText(mContext, R.string.error_live_map, message), Toast.LENGTH_LONG).show()
		);
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

		if (mStateChangeListeners.isEmpty()) {
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
