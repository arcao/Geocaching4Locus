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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.constants.AppConstants;
import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.util.ResourcesExtensionKt;
import com.arcao.geocaching4locus.error.ErrorActivity;
import com.arcao.geocaching4locus.live_map.LiveMapService;
import com.arcao.geocaching4locus.live_map.model.LastLiveMapCoordinates;
import com.arcao.geocaching4locus.live_map.receiver.LiveMapBroadcastReceiver;
import com.arcao.geocaching4locus.live_map.task.LiveMapDownloadTask;
import com.arcao.geocaching4locus.settings.SettingsActivity;
import com.arcao.geocaching4locus.settings.fragment.LiveMapPreferenceFragment;
import locus.api.android.ActionTools;
import locus.api.android.utils.LocusInfo;
import locus.api.android.utils.LocusUtils;
import timber.log.Timber;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

public class LiveMapNotificationManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String VAR_B_MAP_VISIBLE = ("1300");

    private static final String ACTION_HIDE_NOTIFICATION = "com.arcao.geocaching4locus.action.HIDE_NOTIFICATION";
    private static final String ACTION_LIVE_MAP_ENABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_ENABLE";
    private static final String ACTION_LIVE_MAP_DISABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_DISABLE";
    private static final long NOTIFICATION_TIMEOUT_MS = 2200;
    private static final String NOTIFICATION_CHANNEL_ID = "LIVE_MAP_NOTIFICATION_CHANNEL";

    private static boolean notificationShown;
    private static boolean lastLiveMapState;

    private final Context context;
    private final AlarmManager alarmManager;
    private final NotificationManager notificationManager;
    private final SharedPreferences preferences;
    private final boolean showLiveMapDisabledNotification;
    private final boolean showLiveMapVisibleOnlyNotification;

    private final Collection<LiveMapStateChangeListener> stateChangeListeners = new CopyOnWriteArraySet<>();

    @Deprecated
    public static LiveMapNotificationManager get(Context context) {
        return new LiveMapNotificationManager(context);
    }

    public LiveMapNotificationManager(@NonNull Context context) {
        this.context = context.getApplicationContext();

        notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        alarmManager = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);

        showLiveMapDisabledNotification = preferences.getBoolean(PrefConstants.SHOW_LIVE_MAP_DISABLED_NOTIFICATION, false);
        showLiveMapVisibleOnlyNotification = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null) {
            return;
        }

        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                context.getText(R.string.menu_live_map),
                NotificationManager.IMPORTANCE_LOW
        );

        notificationManager.createNotificationChannel(notificationChannel);
    }

    public boolean handleBroadcastIntent(Intent intent) {
        if (intent == null || intent.getAction() == null)
            return false;

        Timber.i(intent.getAction());

        switch (intent.getAction()) {
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

                if (!isMapVisible(intent) && showLiveMapVisibleOnlyNotification) {
                    return false;
                }

                if (!notificationShown || lastLiveMapState != isLiveMapEnabled()) {
                    showNotification();
                    //noinspection AssignmentToStaticFieldFromInstanceMethod
                    lastLiveMapState = isLiveMapEnabled();
                }
                updateNotificationHideAlarm();
                return false;
        }
    }

    private static boolean isMapVisible(Intent intent) {
        return intent.getBooleanExtra(VAR_B_MAP_VISIBLE, false);
    }

    public boolean isForceUpdateRequiredInFuture() {
        return !notificationShown;
    }

    public void setDownloadingProgress(int current, int count) {
        if (!notificationShown)
            return;

        NotificationCompat.Builder nb = createNotification();

        if (current == 0) {
            nb.setProgress(0, 0, true);
        } else if (current < count) {
            nb.setProgress(count, current, false);
        }

        if (current < count) {
            nb.setSmallIcon(R.drawable.ic_stat_live_map_downloading_anim);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                nb.setContentText(ResourcesExtensionKt.getText(context, R.string.notify_live_map_message_downloading, current, count, (current * 100) / count));
            } else {
                nb.setContentTitle(ResourcesExtensionKt.getText(context, R.string.notify_live_map_message_downloading, current, count, (current * 100) / count));
            }
        }

        notificationManager.notify(AppConstants.NOTIFICATION_ID_LIVEMAP, nb.build());
    }

    private void updateNotificationHideAlarm() {
        PendingIntent pendingIntent = createPendingIntent(ACTION_HIDE_NOTIFICATION);

        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + NOTIFICATION_TIMEOUT_MS, pendingIntent);
    }

    private void showNotification() {
        notificationShown = true;

        notificationManager.notify(AppConstants.NOTIFICATION_ID_LIVEMAP, createNotification().build());
    }

    private void hideNotification() {
        notificationShown = false;

        alarmManager.cancel(createPendingIntent(ACTION_HIDE_NOTIFICATION));
        LiveMapService.stop(context);
        notificationManager.cancel(AppConstants.NOTIFICATION_ID_LIVEMAP);
    }

    public NotificationCompat.Builder createNotification() {
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        nb.setOngoing(true);
        nb.setWhen(0); // this fix redraw issue while refreshing
        nb.setLocalOnly(true);
        nb.setCategory(NotificationCompat.CATEGORY_STATUS);
        nb.setPriority(NotificationCompat.PRIORITY_LOW);

        nb.setColor(ContextCompat.getColor(context, R.color.primary));

        CharSequence state;
        if (isLiveMapEnabled()) {
            nb.setSmallIcon(R.drawable.ic_stat_live_map);
            state = context.getText(R.string.notify_live_map_message_enabled);
            nb.addAction(R.drawable.ic_stat_navigation_cancel, context.getText(R.string.notify_live_map_action_disable), createPendingIntent(ACTION_LIVE_MAP_DISABLE));
        } else {
            nb.setSmallIcon(R.drawable.ic_stat_live_map_disabled);
            state = context.getText(R.string.notify_live_map_message_disabled);
            nb.addAction(R.drawable.ic_stat_navigation_accept, context.getText(R.string.notify_live_map_action_enable), createPendingIntent(ACTION_LIVE_MAP_ENABLE));
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                SettingsActivity.createIntent(context, LiveMapPreferenceFragment.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
        nb.addAction(R.drawable.ic_stat_live_map_settings, context.getText(R.string.notify_live_map_action_settings), pendingIntent);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            nb.setContentTitle(context.getText(R.string.notify_live_map));
            nb.setContentText(state);
        } else {
            nb.setSubText(context.getText(R.string.menu_live_map));
            nb.setContentTitle(state);
        }

        return nb;
    }

    private PendingIntent createPendingIntent(String action) {
        Intent intent = new Intent(action, null, context, LiveMapBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    public void setLiveMapEnabled(boolean enabled) {
        boolean periodicUpdateEnabled = true;
        LocusUtils.LocusVersion locusVersion = LocusUtils.getActiveVersion(context);

        if (locusVersion != null) {
            try {
                LocusInfo info = ActionTools.getLocusInfo(context, locusVersion);
                if (info != null) {
                    periodicUpdateEnabled = info.isPeriodicUpdatesEnabled();
                }
            } catch (Throwable e) {
                Timber.e(e,
                        "Unable to receive info about current state of periodic update events from Locus.");
            }
        } else {
            periodicUpdateEnabled = false;
        }

        // hide visible geocaches when live map is disabling
        if (!enabled && isLiveMapEnabled() && preferences.getBoolean(PrefConstants.LIVE_MAP_HIDE_CACHES_ON_DISABLED, false)) {
            LiveMapDownloadTask.cleanMapItems(context);
        }

        if (enabled && !periodicUpdateEnabled) {
            enabled = false;

            showError(R.string.error_live_map_periodic_updates);
        } else if (enabled) {
            Toast.makeText(context, context.getText(R.string.toast_live_map_enabled), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getText(R.string.toast_live_map_disabled), Toast.LENGTH_LONG).show();
            LastLiveMapCoordinates.remove();
        }

        try {
            if (locusVersion != null) {
                // make sure Live Map broadcast receiver is always enabled
                ActionTools.enablePeriodicUpdatesReceiver(context, locusVersion, LiveMapBroadcastReceiver.class);
            }
        } catch (Throwable e) {
            Timber.e(e, "Unable to enable LiveMapBroadcastReceiver.");
        }

        preferences.edit().putBoolean(PrefConstants.LIVE_MAP, enabled).apply();
    }

    private void showError(@StringRes int message) {
        context.startActivity(new ErrorActivity.IntentBuilder(context).message(message).build().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public void showLiveMapError(@StringRes int message) {
        showLiveMapError(context.getText(message));
    }

    public void showLiveMapError(final CharSequence message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, ResourcesExtensionKt.getText(context, R.string.error_live_map, message), Toast.LENGTH_LONG).show()
        );
    }


    public boolean isLiveMapEnabled() {
        return preferences.getBoolean(PrefConstants.LIVE_MAP, false);
    }

    public void addLiveMapStateChangeListener(LiveMapStateChangeListener liveMapStateChangeListener) {
        stateChangeListeners.add(liveMapStateChangeListener);

        if (stateChangeListeners.size() == 1) {
            preferences.registerOnSharedPreferenceChangeListener(this);
        }
    }

    public void removeLiveMapStateChangeListener(LiveMapStateChangeListener liveMapStateChangeListener) {
        stateChangeListeners.remove(liveMapStateChangeListener);

        if (stateChangeListeners.isEmpty()) {
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PrefConstants.LIVE_MAP.equals(key)) {
            for (LiveMapStateChangeListener listener : stateChangeListeners) {
                listener.onLiveMapStateChange(sharedPreferences.getBoolean(key, false));
            }
        }
    }

    public interface LiveMapStateChangeListener {
        void onLiveMapStateChange(boolean newState);
    }
}
