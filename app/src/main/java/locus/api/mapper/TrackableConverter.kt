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
            imgUrl = trackable.iconUrl.orEmpty()
            name = trackable.name
            originalOwner = trackable.owner.username
            currentOwner = trackable.owner.username
            srcDetails = trackable.url.orEmpty()
            released = trackable.releasedDate.toEpochMilli()
            origin = trackable.originCountry.orEmpty()

            if (!trackableLightData) {
                details = trackable.description.orEmpty()
                goal = trackable.goal.orEmpty()
            }
        }
    }
}
