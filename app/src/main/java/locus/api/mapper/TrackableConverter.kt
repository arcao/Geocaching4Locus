package locus.api.mapper

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.arcao.geocaching.api.data.Trackable
import locus.api.objects.extra.Point
import locus.api.objects.geocaching.GeocachingTrackable
import locus.api.utils.addIgnoreNull
import locus.api.utils.isNullOrEmpty
import locus.api.utils.toTime

class TrackableConverter {
    fun addTrackables(@NonNull point: Point, @Nullable trackables: Collection<Trackable>?) {
        if (point.gcData?.trackables?.isEmpty() != false || trackables.isNullOrEmpty())
            return

        val trackableLightData = trackables!!.size > 100
        for (trackable in trackables) {
            point.gcData.trackables.addIgnoreNull(createLocusGeocachingTrackable(trackable, trackableLightData))
        }
    }

    @Nullable
    private fun createLocusGeocachingTrackable(@Nullable trackable: Trackable?, trackableLightData: Boolean): GeocachingTrackable? {
        if (trackable == null)
            return null

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
