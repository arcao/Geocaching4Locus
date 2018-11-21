package locus.api.mapper

import android.content.Context
import com.arcao.geocaching.api.data.Geocache
import com.arcao.geocaching.api.data.GeocacheLog
import com.arcao.geocaching.api.data.Trackable
import locus.api.objects.extra.Point
import locus.api.utils.addIgnoreNull

class DataMapper(context: Context) {
    private val geocacheConverter = GeocacheConverter(context)
    private val geocacheLogConverter = geocacheConverter.geocacheLogConverter
    private val trackableConverter = geocacheConverter.trackableConverter

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
