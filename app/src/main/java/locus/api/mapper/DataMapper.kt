package locus.api.mapper

import com.arcao.geocaching4locus.data.api.model.Geocache
import com.arcao.geocaching4locus.data.api.model.GeocacheLog
import com.arcao.geocaching4locus.data.api.model.Trackable
import locus.api.objects.extra.Point
import locus.api.utils.addIgnoreNull

class DataMapper(
    private val geocacheConverter: GeocacheConverter,
    private val geocacheLogConverter: GeocacheLogConverter,
    private val trackableConverter: TrackableConverter
) {
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
