package com.arcao.geocaching4locus.live_map.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arcao.geocaching4locus.live_map.LiveMapService
import com.arcao.geocaching4locus.live_map.model.LastLiveMapCoordinates
import com.arcao.geocaching4locus.live_map.util.LiveMapNotificationManager
import locus.api.android.ActionBasics
import locus.api.android.utils.LocusUtils
import locus.api.extension.isInvalid
import locus.api.objects.extra.Location
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class LiveMapBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val notificationManager by inject<LiveMapNotificationManager>()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == null)
            return

        if (notificationManager.handleBroadcastIntent(intent)) {
            forceUpdate = notificationManager.isForceUpdateRequiredInFuture
            return
        }

        if (!notificationManager.isLiveMapEnabled) {
            return
        }

        val container =
            ActionBasics.getUpdateContainer(context, requireNotNull(LocusUtils.createLocusVersion(context, intent)))
                ?: return

        // ignore onTouch events
        if (container.isUserTouching)
            return

        // sending data back to Locus based on events if has a new map center or zoom level and map is visible
        if (!container.isMapVisible)
            return

        val mapTopLeft = container.mapTopLeft
        val mapCenter = container.locMapCenter
        val mapBottomRight = container.mapBottomRight

        // When Live map is enabled, Locus sometimes send NaN when is starting
        if (mapTopLeft.isInvalid() || mapCenter.isInvalid() || mapBottomRight.isInvalid())
            return

        LastLiveMapCoordinates.update(mapCenter, mapTopLeft, mapBottomRight)

        if (!isNewMapCenter(mapCenter, mapTopLeft) && container.mapZoomLevel == lastMapZoomLevel && !forceUpdate)
            return

        lastMapCenter = mapCenter
        lastMapZoomLevel = container.mapZoomLevel
        forceUpdate = false

        // check if zoom level is too low
        if (mapTopLeft.distanceTo(mapBottomRight) >= MAX_DIAGONAL_DISTANCE)
            return

        // Start service to retrieve caches
        LiveMapService.start(
            context,
            mapCenter.getLatitude(),
            mapCenter.getLongitude(),
            mapTopLeft.getLatitude(),
            mapTopLeft.getLongitude(),
            mapBottomRight.getLatitude(),
            mapBottomRight.getLongitude()
        )
    }

    companion object {
        // Limitation on Groundspeak side to 100000 meters
        private const val MAX_DIAGONAL_DISTANCE = 100000f
        private const val DISTANCE_LIMIT_DIVIDER = 2.5f

        internal var forceUpdate: Boolean = false
        private var lastMapCenter: Location? = null
        private var lastMapZoomLevel: Int = -1

        private fun isNewMapCenter(mapCenter: Location, mapTopLeft: Location): Boolean {
            val mc = lastMapCenter
            return mc != null && mc.distanceTo(mapCenter) < computeNotificationLimit(mapCenter, mapTopLeft)
        }

        private fun computeNotificationLimit(mapCenter: Location, mapTopLeft: Location): Float {
            return mapTopLeft.distanceTo(mapCenter) / DISTANCE_LIMIT_DIVIDER
        }
    }
}