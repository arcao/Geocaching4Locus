package com.arcao.geocaching4locus.live_map

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
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

        viewModel.progress.withObserve(this, ::handleProgress)

        lifecycle.addObserver(viewModel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // in case the service is already running, this must be called after each startForegroundService
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    AppConstants.NOTIFICATION_ID_LIVEMAP,
                    notificationManager.createNotification().build(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(
                    AppConstants.NOTIFICATION_ID_LIVEMAP,
                    notificationManager.createNotification().build()
                )
            }
        } catch (e: Exception) {
            // if service is restarted after app kill, startForeground may crash on Android 14
            Timber.e(e)
        }

        if (intent != null) {
            if (ACTION_START == intent.action) {
                viewModel.addTask(intent, onCompleteCallback)
            } else if (ACTION_STOP == intent.action) {
                cancelTasks()
                stopSelf(startId)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun cancelTasks() {
        viewModel.cancelTasks()
        ServiceUtil.releaseAllWakeLocks(ComponentName(this, LiveMapService::class.java))
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        cancelTasks()
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
        private val ACTION_START = LiveMapService::class.java.canonicalName!! + ".START"
        private val ACTION_STOP = LiveMapService::class.java.canonicalName!! + ".STOP"

        fun stop(context: Context) {
            context.stopService(Intent(context, LiveMapService::class.java).setAction(ACTION_STOP))
        }

        fun start(
            context: Context,
            centerLatitude: Double,
            centerLongitude: Double,
            topLeftLatitude: Double,
            topLeftLongitude: Double,
            bottomRightLatitude: Double,
            bottomRightLongitude: Double
        ) = ServiceUtil.startWakefulForegroundService(context,
            Intent(context, LiveMapService::class.java).apply {
                action = ACTION_START
                putExtra(PARAM_LATITUDE, centerLatitude)
                putExtra(PARAM_LONGITUDE, centerLongitude)
                putExtra(PARAM_TOP_LEFT_LATITUDE, topLeftLatitude)
                putExtra(PARAM_TOP_LEFT_LONGITUDE, topLeftLongitude)
                putExtra(PARAM_BOTTOM_RIGHT_LATITUDE, bottomRightLatitude)
                putExtra(PARAM_BOTTOM_RIGHT_LONGITUDE, bottomRightLongitude)
            }
        )
    }
}
