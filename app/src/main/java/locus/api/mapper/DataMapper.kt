package locus.api.mapper

import android.content.Context
import com.arcao.geocaching.api.data.Geocache
import com.arcao.geocaching.api.data.GeocacheLog
import com.arcao.geocaching.api.data.Trackable
import com.arcao.geocaching4locus.settings.manager.DefaultPreferenceManager
import locus.api.objects.extra.Point
import locus.api.utils.addIgnoreNull

class DataMapper(
    private val geocacheConverter: GeocacheConverter,
    private val geocacheLogConverter: GeocacheLogConverter,
    private val trackableConverter: TrackableConverter
) {

    @Deprecated("Use koin.")
    constructor(context: Context) : this(
        GeocacheConverter(
            context,
            DefaultPreferenceManager(context),
            GeocacheLogConverter(
                ImageDataConverter()
            ),
            ImageDataConverter(),
            TrackableConverter(),
            WaypointConverter()
        ),
        GeocacheLogConverter(
            ImageDataConverter()
        ),
        TrackableConverter()
    )

    fun createLocusPoints(geocaches: Collection<Geocache>): List<Point> {
        if (geocaches.isEmpty())
            return emptyList()

        val points = ArrayList<Point>(geocaches.size)
        for (cache in geocaches) {
            points.addIgnoreNull(createLocusPoint(cache))
        }

        return points
    }

    fun createLocusPoint(geocache: Geocache): Point {
        return geocacheConverter.createLocusPoint(geocache)
    }

    fun addCacheLogs(waypoint: Point, logs: Collection<GeocacheLog>) {
        geocacheLogConverter.addGeocacheLogs(waypoint, logs)
    }

    fun addTrackables(waypoint: Point, trackables: Collection<Trackable>) {
        trackableConverter.addTrackables(waypoint, trackables)
    }
}
