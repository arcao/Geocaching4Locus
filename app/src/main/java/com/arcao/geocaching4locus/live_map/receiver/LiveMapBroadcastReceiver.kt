package com.arcao.geocaching4locus.live_map.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arcao.geocaching4locus.live_map.LiveMapService
import com.arcao.geocaching4locus.live_map.model.LastLiveMapCoordinates
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import locus.api.android.features.periodicUpdates.PeriodicUpdatesHandler
import locus.api.android.features.periodicUpdates.UpdateContainer
import locus.api.android.utils.LocusUtils
import locus.api.objects.extra.Location
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class LiveMapBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val notificationManager by inject<LiveMapNotificationManager>()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == null)
            return

        LastLiveMapCoordinates.update(intent)

        if (notificationManager.handleBroadcastIntent(intent)) {
            forceUpdate = notificationManager.isForceUpdateRequiredInFuture
            return
        }

        if (!notificationManager.isLiveMapEnabled) {
            return
        }

        // ignore onTouch events
        if (intent.getBooleanExtra(VAR_B_MAP_USER_TOUCHES, false))
            return

        // temporary fix for NPE bug (locMapCenter can be null)
        if (LocusUtils.getLocationFromIntent(intent, VAR_LOC_MAP_CENTER).isInvalid())
            return

        // get valid instance of PeriodicUpdate object
        val handler = PeriodicUpdatesHandler.getInstance()

        // call onUpdate only when distance between old and new location is greater than computeNotificationLimit(..)
        handler.setLocNotificationLimit(computeNotificationLimit(intent))

        // handle event
        handler.onReceive(context, intent, object : PeriodicUpdatesHandler.OnUpdate {
            override fun onIncorrectData() {}

            override fun onUpdate(locusVersion: LocusUtils.LocusVersion, update: UpdateContainer) {
                // sending data back to Locus based on events if has a new map center or zoom level and map is visible
                if (!update.isMapVisible)
                    return

                if (!update.isNewMapCenter && !update.isNewZoomLevel && !forceUpdate)
                    return


                forceUpdate = false

                // When Live map is enabled, Locus sometimes send NaN when is starting
                if (update.mapTopLeft.isInvalid() || update.mapBottomRight.isInvalid())
                    return

                if (update.mapTopLeft.distanceTo(update.mapBottomRight) >= MAX_DIAGONAL_DISTANCE)
                    return  // Zoom is too low

                // Start service to download caches
                LiveMapService.start(
                    context,
                    update.locMapCenter.getLatitude(),
                    update.locMapCenter.getLongitude(),
                    update.mapTopLeft.getLatitude(),
                    update.mapTopLeft.getLongitude(),
                    update.mapBottomRight.getLatitude(),
                    update.mapBottomRight.getLongitude()
                )
            }
        })
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun Location?.isInvalid() = this == null || getLatitude().isNaN() || getLongitude().isNaN()

    companion object {
        private const val VAR_B_MAP_USER_TOUCHES = "1306"
        private const val VAR_LOC_MAP_CENTER = "1302"
        private const val VAR_LOC_MAP_BBOX_TOP_LEFT = "1303"

        // Limitation on Groundspeak side to 100000 meters
        private const val MAX_DIAGONAL_DISTANCE = 100000f
        private const val DEFAULT_DISTANCE_LIMIT = 100.0
        private const val DISTANCE_LIMIT_DIVIDER = 2.5

        internal var forceUpdate: Boolean = false

        private fun computeNotificationLimit(i: Intent): Double {
            val locMapCenter = LocusUtils.getLocationFromIntent(i, VAR_LOC_MAP_CENTER)
            val mapTopLeft = LocusUtils.getLocationFromIntent(i, VAR_LOC_MAP_BBOX_TOP_LEFT)

            return if (locMapCenter == null || mapTopLeft == null) {
                DEFAULT_DISTANCE_LIMIT
            } else {
                mapTopLeft.distanceTo(locMapCenter) / DISTANCE_LIMIT_DIVIDER
            }
        }
    }
}
