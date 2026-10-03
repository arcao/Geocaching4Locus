package com.arcao.geocaching4locus.live_map

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import com.arcao.geocaching4locus.base.ProgressState
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.base.util.ServiceUtil
import com.arcao.geocaching4locus.base.util.withObserve
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import org.koin.android.ext.android.inject
import timber.log.Timber

class LiveMapService : LifecycleService() {
    private val notificationManager by inject<LiveMapNotificationManager>()
    private val viewModel by inject<LiveMapViewModel>()
    private val onCompleteCallback: (Intent) -> Unit = { ServiceUtil.completeWakefulIntent(it) }

    override fun onCreate() {
        super.onCreate()

        isRunning = true

        viewModel.progress.withObserve(this, ::handleProgress)

        lifecycle.addObserver(viewModel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // The service is restarted by the system with null intent after the app was killed. Live map
        // is not resumed automatically, user is notified by a notification and have to resume it.
        if (intent == null) {
            stopSelf(startId)
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_ENABLE -> {
                if (!startForegroundSafely()) {
                    stopSelf(startId)
                    return START_NOT_STICKY
                }

                // Enable Live map, the user's action allows to start the foreground service
                notificationManager.isLiveMapEnabled = true
                if (!notificationManager.isLiveMapEnabled) {
                    stopSelf(startId)
                }
            }

            ACTION_UPDATE -> {
                if (!notificationManager.isLiveMapEnabled) {
                    stopSelf(startId)
                } else {
                    viewModel.addTask(intent, onCompleteCallback)
                }
            }

            else -> stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    private fun startForegroundSafely(): Boolean {
        return try {
            ServiceCompat.startForeground(
                this,
                AppConstants.NOTIFICATION_ID_LIVEMAP,
                notificationManager.createNotification().build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
            notificationManager.onForegroundServiceStarted()
            true
        } catch (e: Exception) {
            // ForegroundServiceStartNotAllowedException, e.g. the dataSync time limit was reached
            // and the user hasn't brought the app to foreground yet
            Timber.e(e)
            notificationManager.isLiveMapEnabled = false
            false
        }
    }

    /**
     * Android 15+ limits `dataSync` foreground services to 6 hours in 24 hours. The service must
     * stop itself after this callback, otherwise the system throws an exception. The user is
     * notified and can resume the Live map after bringing the app to foreground.
     */
    override fun onTimeout(startId: Int, fgsType: Int) {
        Timber.w("LiveMap foreground service timed out (type=%d), stopping", fgsType)
        cancelTasks()
        stopSelf(startId)
        notificationManager.showPausedNotification()
    }

    private fun cancelTasks() {
        viewModel.cancelTasks()
        ServiceUtil.releaseAllWakeLocks(ComponentName(this, LiveMapService::class.java))
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        isRunning = false
        cancelTasks()
        notificationManager.onForegroundServiceStopped()
        super.onDestroy()
    }

    private fun handleProgress(state: ProgressState) {
        when (state) {
            is ProgressState.ShowProgress -> {
                notificationManager.setDownloadingProgress(state.progress, state.maxProgress)
            }

            ProgressState.HideProgress -> {
                notificationManager.setDownloadingProgress(Int.MAX_VALUE, Int.MAX_VALUE)
            }
        }
    }

    companion object {
        const val PARAM_LATITUDE = "LATITUDE"
        const val PARAM_LONGITUDE = "LONGITUDE"
        const val PARAM_TOP_LEFT_LATITUDE = "TOP_LEFT_LATITUDE"
        const val PARAM_TOP_LEFT_LONGITUDE = "TOP_LEFT_LONGITUDE"
        const val PARAM_BOTTOM_RIGHT_LATITUDE = "BOTTOM_RIGHT_LATITUDE"
        const val PARAM_BOTTOM_RIGHT_LONGITUDE = "BOTTOM_RIGHT_LONGITUDE"
        private val ACTION_ENABLE = LiveMapService::class.java.canonicalName!! + ".ENABLE"
        private val ACTION_UPDATE = LiveMapService::class.java.canonicalName!! + ".UPDATE"

        /**
         * True while the service runs, which means Live map is running.
         */
        @Volatile
        var isRunning: Boolean = false
            private set

        fun stop(context: Context) {
            context.stopService(Intent(context, LiveMapService::class.java))
        }

        /**
         * Creates an intent which enables Live map and starts the foreground service. It must be
         * sent from a context where the start of a foreground service is allowed: visible app or
         * a user's action on a notification ([android.app.PendingIntent.getForegroundService]).
         */
        fun createEnableIntent(context: Context) =
            Intent(context, LiveMapService::class.java).setAction(ACTION_ENABLE)

        fun enable(context: Context) {
            context.startForegroundService(createEnableIntent(context))
        }

        /**
         * Sends new map coordinates to the running service. The foreground service must be
         * already running, the foreground service can't be started from a background.
         */
        fun update(
            context: Context,
            centerLatitude: Double,
            centerLongitude: Double,
            topLeftLatitude: Double,
            topLeftLongitude: Double,
            bottomRightLatitude: Double,
            bottomRightLongitude: Double
        ) = ServiceUtil.startWakefulService(
            context,
            Intent(context, LiveMapService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(PARAM_LATITUDE, centerLatitude)
                putExtra(PARAM_LONGITUDE, centerLongitude)
                putExtra(PARAM_TOP_LEFT_LATITUDE, topLeftLatitude)
                putExtra(PARAM_TOP_LEFT_LONGITUDE, topLeftLongitude)
                putExtra(PARAM_BOTTOM_RIGHT_LATITUDE, bottomRightLatitude)
                putExtra(PARAM_BOTTOM_RIGHT_LONGITUDE, bottomRightLongitude)
            },
            foreground = false
        )
    }
}
