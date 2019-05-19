package locus.api.mapper

import com.arcao.geocaching4locus.data.api.model.Trackable
import locus.api.objects.extra.Point
import locus.api.objects.geocaching.GeocachingTrackable

class TrackableConverter {
    fun addTrackables(point: Point, trackables: Collection<Trackable>) {
        if (point.gcData?.trackables == null || trackables.isEmpty())
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
            id = trackable.id
            imgUrl = trackable.iconUrl
            name = trackable.name
            currentOwner = trackable.holder?.username
            originalOwner = trackable.owner.username
            srcDetails = trackable.url
            released = trackable.releasedDate.toEpochMilli()

            if (!trackableLightData) {
                details = trackable.description
                goal = trackable.goal
            }
        }
    }
}
