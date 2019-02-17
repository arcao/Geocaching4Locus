package locus.api.mapper

import com.arcao.geocaching.api.data.Trackable
import locus.api.objects.extra.Point
import locus.api.objects.geocaching.GeocachingTrackable
import locus.api.utils.toTime

class TrackableConverter {
    fun addTrackables(point: Point, trackables: Collection<Trackable>) {
        if (point.gcData?.trackables?.isEmpty() != false || trackables.isEmpty())
            return

        point.gcData.trackables.addAll(createLocusGeocachingTrackables(trackables))
    }

    fun createLocusGeocachingTrackables(trackables: Collection<Trackable>): Collection<GeocachingTrackable> {
        val lightData = trackables.size >= 100

        return trackables.map {
            createLocusGeocachingTrackable(it, lightData)
        }
    }

    private fun createLocusGeocachingTrackable(trackable: Trackable, trackableLightData: Boolean): GeocachingTrackable {
        return GeocachingTrackable().apply {
            id = trackable.id()
            imgUrl = trackable.trackableTypeImage()
            name = trackable.name()
            currentOwner = trackable.currentOwner()?.userName()
            originalOwner = trackable.owner()?.userName()
            srcDetails = trackable.trackableUrl()
            released = trackable.created().toTime()

            if (!trackableLightData) {
                details = trackable.description()
                goal = trackable.goal()
            }
        }
    }
}
