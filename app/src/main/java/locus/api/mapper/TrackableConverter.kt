package locus.api.mapper

import com.arcao.geocaching4locus.data.api.model.Trackable
import locus.api.objects.geoData.Point
import locus.api.objects.geocaching.GeocachingTrackable

class TrackableConverter {
    fun addTrackables(point: Point, trackables: Collection<Trackable>) {
        val gcData = point.gcData ?: return

        if (trackables.isEmpty())
            return

        gcData.trackables.addAll(createLocusGeocachingTrackables(trackables))
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
            originalOwner = trackable.owner.username.orEmpty()
            currentOwner = trackable.owner.username.orEmpty()
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
