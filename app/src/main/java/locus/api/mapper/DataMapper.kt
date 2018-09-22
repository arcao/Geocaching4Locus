package locus.api.mapper

import android.content.Context
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.arcao.geocaching.api.data.Geocache
import com.arcao.geocaching.api.data.GeocacheLog
import com.arcao.geocaching.api.data.Trackable
import locus.api.objects.extra.Waypoint
import locus.api.utils.addIgnoreNull
import java.util.*

class DataMapper(@NonNull context: Context) {
    private val geocacheConverter = GeocacheConverter(context)
    private val geocacheLogConverter = geocacheConverter.geocacheLogConverter
    private val trackableConverter = geocacheConverter.trackableConverter

    @NonNull
    fun createLocusWaypoints(@Nullable geocaches: Collection<Geocache>): List<Waypoint> {
        if (geocaches.isEmpty())
            return emptyList()

        val points = ArrayList<Waypoint>(geocaches.size)
        for (cache in geocaches) {
            points.addIgnoreNull(createLocusWaypoint(cache))
        }

        return points
    }

    @Nullable
    fun createLocusWaypoint(@NonNull geocache: Geocache): Waypoint? {
        return geocacheConverter.createLocusWaypoint(geocache)
    }

    fun addCacheLogs(@NonNull waypoint: Waypoint, @Nullable logs: Collection<GeocacheLog>?) {
        geocacheLogConverter.addGeocacheLogs(waypoint, logs)
    }

    fun addTrackables(@NonNull waypoint: Waypoint, @Nullable trackables: Collection<Trackable>?) {
        trackableConverter.addTrackables(waypoint, trackables)
    }
}
