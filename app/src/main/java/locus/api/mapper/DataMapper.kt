package locus.api.mapper

import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.arcao.geocaching.api.data.Geocache
import com.arcao.geocaching.api.data.GeocacheLog
import com.arcao.geocaching.api.data.Trackable
import locus.api.objects.extra.Point
import locus.api.utils.addIgnoreNull
import java.util.*

class DataMapper(@NonNull context: Context) {
    private val geocacheConverter = GeocacheConverter(context)
    private val geocacheLogConverter = geocacheConverter.geocacheLogConverter
    private val trackableConverter = geocacheConverter.trackableConverter

    @NonNull
    fun createLocusPoints(@Nullable geocaches: Collection<Geocache>): List<Point> {
        if (geocaches.isEmpty())
            return emptyList()

        val points = ArrayList<Point>(geocaches.size)
        for (cache in geocaches) {
            points.addIgnoreNull(createLocusPoint(cache))
        }

        return points
    }

    @Nullable
    fun createLocusPoint(@NonNull geocache: Geocache): Point? {
        return geocacheConverter.createLocusPoint(geocache)
    }

    fun addCacheLogs(@NonNull waypoint: Point, @Nullable logs: Collection<GeocacheLog>?) {
        geocacheLogConverter.addGeocacheLogs(waypoint, logs)
    }

    fun addTrackables(@NonNull waypoint: Point, @Nullable trackables: Collection<Trackable>?) {
        trackableConverter.addTrackables(waypoint, trackables)
    }
}
