package com.arcao.geocaching4locus.live_map.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.constants.PrefConstants
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import com.arcao.geocaching4locus.base.usecase.RemoveLocusMapPointsUseCase
import com.arcao.geocaching4locus.base.util.getText
import com.arcao.geocaching4locus.error.ErrorActivity
import com.arcao.geocaching4locus.live_map.LiveMapService
import com.arcao.geocaching4locus.live_map.model.LastLiveMapCoordinates
import com.arcao.geocaching4locus.live_map.receiver.LiveMapBroadcastReceiver
import com.arcao.geocaching4locus.settings.SettingsActivity
import com.arcao.geocaching4locus.settings.fragment.LiveMapPreferenceFragment
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import locus.api.manager.LocusMapManager
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArraySet

class LiveMapNotificationManager(
    private val context: Context,
    private val defaultPreferenceManager: DefaultPreferenceManager,
    private val removeLocusMapPoints: RemoveLocusMapPointsUseCase,
    private val locusMapManager: LocusMapManager,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val stateChangeListeners = CopyOnWriteArraySet<LiveMapStateChangeListener>()

    val isForceUpdateRequiredInFuture: Boolean
        get() = !notificationShown

    var isLiveMapEnabled: Boolean
        get() = preferences.getBoolean(PrefConstants.LIVE_MAP, false)
        set(enabled) {
            var willBeEnabled = enabled
            val periodicUpdateEnabled = locusMapManager.periodicUpdateEnabled

            if (!willBeEnabled && isLiveMapEnabled && defaultPreferenceManager.hideGeocachesOnLiveMapDisabled) {
                // hide visible geocaches when live map is disabling
                removeLiveMapItems()
            }

            when {
                willBeEnabled && !periodicUpdateEnabled -> {
                    willBeEnabled = false
                    showError(R.string.error_live_map_periodic_updates)
                }

                willBeEnabled -> showLiveMapToast(R.string.toast_live_map_enabled)

                else -> {
                    showLiveMapToast(R.string.toast_live_map_disabled)
                    LastLiveMapCoordinates.remove()
                }
            }

            // make sure Live Map broadcast receiver is always enabled
            locusMapManager.enablePeriodicUpdatesReceiver(LiveMapBroadcastReceiver::class)

            preferences.edit {
                putBoolean(PrefConstants.LIVE_MAP, willBeEnabled)
            }
        }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channelDescription = context.getText(R.string.menu_live_map)

        // update current channel on locale change only
        val currentChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        if (currentChannel != null && currentChannel.description == channelDescription) {
            return
        }

        val newChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelDescription,
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(newChannel)
    }

    fun handleBroadcastIntent(intent: Intent?): Boolean {
        if (intent?.action == null)
            return false

        Timber.i(intent.action)

        when (intent.action) {
            ACTION_HIDE_NOTIFICATION -> {
                hideNotification()
                return true
            }

            ACTION_LIVE_MAP_ENABLE -> {
                isLiveMapEnabled = true
                showNotification()
                return true
            }

            ACTION_LIVE_MAP_DISABLE -> {
                isLiveMapEnabled = false
                if (defaultPreferenceManager.showLiveMapDisabledNotification) {
                    showNotification()
                } else {
                    hideNotification()
                }
                return true
            }
            else -> {
                if (!isLiveMapEnabled && !defaultPreferenceManager.showLiveMapDisabledNotification) {
                    return false
                }

                if (!isMapVisible(intent) && defaultPreferenceManager.showLiveMapDisabledNotification) {
                    return false
                }

                if (!notificationShown || lastLiveMapState != isLiveMapEnabled) {
                    showNotification()
                    lastLiveMapState = isLiveMapEnabled
                }

                updateNotificationHideAlarm()

                return false
            }
        }
    }

    fun setDownloadingProgress(current: Int, count: Int) {
        if (!notificationShown)
            return

        val nb = createNotification()

        if (current == 0) {
            nb.setProgress(0, 0, true)
        } else if (current < count) {
            nb.setProgress(count, current, false)
        }

        if (current < count) {
            nb.priority = NotificationCompat.PRIORITY_DEFAULT

            nb.setSmallIcon(R.drawable.ic_stat_live_map_downloading_anim)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                nb.setContentText(
                    context.getText(
                        R.string.notify_live_map_message_downloading,
                        current,
                        count,
                        current * 100 / count
                    )
                )
            } else {
                nb.setContentTitle(
                    context.getText(
                        R.string.notify_live_map_message_downloading,
                        current,
                        count,
                        current * 100 / count
                    )
                )
            }
        }

        notificationManager.notify(AppConstants.NOTIFICATION_ID_LIVEMAP, nb.build())
    }

    private fun updateNotificationHideAlarm() {
        val pendingIntent = createPendingBroadcastIntent(ACTION_HIDE_NOTIFICATION)

        alarmManager.cancel(pendingIntent)
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + NOTIFICATION_TIMEOUT_MS,
            pendingIntent
        )
    }

    private fun showNotification() {
        notificationShown = true
        notificationManager.notify(AppConstants.NOTIFICATION_ID_LIVEMAP, createNotification().build())
    }

    private fun hideNotification() {
        notificationShown = false

        alarmManager.cancel(createPendingBroadcastIntent(ACTION_HIDE_NOTIFICATION))
        LiveMapService.stop(context)
        notificationManager.cancel(AppConstants.NOTIFICATION_ID_LIVEMAP)
    }

    fun createNotification(): NotificationCompat.Builder {
        val nb = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

        nb.setOngoing(true)
        nb.setWhen(0) // this fix redraw issue while refreshing
        nb.setLocalOnly(true)
        nb.setCategory(NotificationCompat.CATEGORY_STATUS)
        nb.priority = NotificationCompat.PRIORITY_LOW
        nb.color = ContextCompat.getColor(context, R.color.primary)

        val state = if (isLiveMapEnabled) {
            nb.setSmallIcon(R.drawable.ic_stat_live_map)
            nb.addAction(
                R.drawable.ic_stat_navigation_cancel,
                context.getText(R.string.notify_live_map_action_disable),
                createPendingBroadcastIntent(ACTION_LIVE_MAP_DISABLE)
            )
            context.getText(R.string.notify_live_map_message_enabled)
        } else {
            nb.setSmallIcon(R.drawable.ic_stat_live_map_disabled)
            nb.addAction(
                R.drawable.ic_stat_navigation_accept,
                context.getText(R.string.notify_live_map_action_enable),
                createPendingBroadcastIntent(ACTION_LIVE_MAP_ENABLE)
            )
            context.getText(R.string.notify_live_map_message_disabled)
        }

        val pendingIntent =
            createPendingActivityIntent(SettingsActivity.createIntent(context, LiveMapPreferenceFragment::class.java))
        nb.addAction(
            R.drawable.ic_stat_live_map_settings,
            context.getText(R.string.notify_live_map_action_settings),
            pendingIntent
        )

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            nb.setContentTitle(context.getText(R.string.notify_live_map))
            nb.setContentText(state)
        } else {
            nb.setSubText(context.getText(R.string.menu_live_map))
            nb.setContentTitle(state)
        }

        return nb
    }

    private fun createPendingActivityIntent(intent: Intent): PendingIntent {
        return PendingIntent.getActivity(
            context, 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun createPendingBroadcastIntent(action: String): PendingIntent {
        val intent = Intent(action, null, context, LiveMapBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }

    private fun showError(@StringRes message: Int) {
        context.startActivity(
            ErrorActivity.IntentBuilder(context)
                .message(message)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    fun showLiveMapToast(@StringRes message: Int) {
        showLiveMapToast(context.getText(message))
    }

    fun showLiveMapToast(message: CharSequence) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(
                context,
                context.getText(R.string.error_live_map, message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun addLiveMapStateChangeListener(liveMapStateChangeListener: LiveMapStateChangeListener) {
        stateChangeListeners.add(liveMapStateChangeListener)

        if (stateChangeListeners.size == 1) {
            preferences.registerOnSharedPreferenceChangeListener(this)
        }
    }

    fun removeLiveMapStateChangeListener(liveMapStateChangeListener: LiveMapStateChangeListener) {
        stateChangeListeners.remove(liveMapStateChangeListener)

        if (stateChangeListeners.isEmpty()) {
            preferences.unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (PrefConstants.LIVE_MAP == key) {
            for (listener in stateChangeListeners) {
                listener.onLiveMapStateChange(sharedPreferences.getBoolean(key, false))
            }
        }
    }

    private fun removeLiveMapItems() = GlobalScope.launch(dispatcherProvider.computation) {
        val lastRequests = defaultPreferenceManager.liveMapLastRequests
        if (lastRequests > 0) {
            removeLocusMapPoints(AppConstants.LIVEMAP_PACK_WAYPOINT_PREFIX, 1, lastRequests)
            defaultPreferenceManager.liveMapLastRequests = 0
        }
    }

    interface LiveMapStateChangeListener {
        fun onLiveMapStateChange(newState: Boolean)
    }

    companion object {
        private const val VAR_B_MAP_VISIBLE = "1300"

        private const val ACTION_HIDE_NOTIFICATION = "com.arcao.geocaching4locus.action.HIDE_NOTIFICATION"
        private const val ACTION_LIVE_MAP_ENABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_ENABLE"
        private const val ACTION_LIVE_MAP_DISABLE = "com.arcao.geocaching4locus.action.LIVE_MAP_DISABLE"
        private const val NOTIFICATION_TIMEOUT_MS: Long = 2200
        private const val NOTIFICATION_CHANNEL_ID = "LIVE_MAP_NOTIFICATION_CHANNEL"

        private var notificationShown: Boolean = false
        private var lastLiveMapState: Boolean = false

        @Deprecated("Use koin.", ReplaceWith("get<LiveMapNotificationManager>()"))
        operator fun get(context: Context): LiveMapNotificationManager {
            return LiveMapNotificationManager(
                context,
                DefaultPreferenceManager(context),
                RemoveLocusMapPointsUseCase(
                    LocusMapManager(context),
                    CoroutinesDispatcherProvider()
                ),
                LocusMapManager(context),
                CoroutinesDispatcherProvider()
            )
        }

        private fun isMapVisible(intent: Intent): Boolean {
            return intent.getBooleanExtra(VAR_B_MAP_VISIBLE, false)
        }
    }
}
