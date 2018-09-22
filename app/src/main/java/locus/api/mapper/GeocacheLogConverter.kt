package locus.api.mapper

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.arcao.geocaching.api.data.GeocacheLog
import com.arcao.geocaching.api.data.type.GeocacheLogType
import locus.api.objects.extra.Waypoint
import locus.api.objects.geocaching.GeocachingLog
import locus.api.utils.addIgnoreNull
import locus.api.utils.toTime
import java.util.*

class GeocacheLogConverter(private val imageDataConverter: ImageDataConverter) {

    fun addGeocacheLogs(@NonNull waypoint: Waypoint, @Nullable logs: Collection<GeocacheLog>?) {
        if (waypoint.gcData.logs?.isEmpty() != false || logs?.isEmpty() != false)
            return

        for (log in logs) {
            waypoint.gcData.logs.addIgnoreNull(createLocusGeocachingLog(log))
        }

        sortLocusGeocachingLogsByDate(waypoint)
    }

    @Nullable
    private fun createLocusGeocachingLog(@Nullable log: GeocacheLog?): GeocachingLog? {
        if (log == null)
            return null

        return GeocachingLog().apply {
            id = log.id()
            date = log.visited().toTime()
            logText = log.text()
            type = createLocusCacheLogType(log.logType())

            val author = log.author()
            if (author != null) {
                finder = author.userName()
                findersFound = author.findCount()
                findersId = author.id()
            }

            for (image in log.images()) {
                addImage(imageDataConverter.createLocusGeocachingImage(image))
            }
            if (log.updatedCoordinates() != null) {
                cooLat = log.updatedCoordinates().latitude()
                cooLon = log.updatedCoordinates().longitude()
            }
        }
    }


    private fun createLocusCacheLogType(@Nullable logType: GeocacheLogType?): Int {
        return when (logType) {
            GeocacheLogType.Announcement -> GeocachingLog.CACHE_LOG_TYPE_ANNOUNCEMENT
            GeocacheLogType.Attended -> GeocachingLog.CACHE_LOG_TYPE_ATTENDED
            GeocacheLogType.DidntFindIt -> GeocachingLog.CACHE_LOG_TYPE_NOT_FOUND
            GeocacheLogType.EnableListing -> GeocachingLog.CACHE_LOG_TYPE_ENABLE_LISTING
            GeocacheLogType.FoundIt -> GeocachingLog.CACHE_LOG_TYPE_FOUND
            GeocacheLogType.NeedsArchived -> GeocachingLog.CACHE_LOG_TYPE_NEEDS_ARCHIVED
            GeocacheLogType.NeedsMaintenance -> GeocachingLog.CACHE_LOG_TYPE_NEEDS_MAINTENANCE
            GeocacheLogType.OwnerMaintenance -> GeocachingLog.CACHE_LOG_TYPE_OWNER_MAINTENANCE
            GeocacheLogType.PostReviewerNote -> GeocachingLog.CACHE_LOG_TYPE_POST_REVIEWER_NOTE
            GeocacheLogType.PublishListing -> GeocachingLog.CACHE_LOG_TYPE_PUBLISH_LISTING
            GeocacheLogType.RetractListing -> GeocachingLog.CACHE_LOG_TYPE_RETRACT_LISTING
            GeocacheLogType.TemporarilyDisableListing -> GeocachingLog.CACHE_LOG_TYPE_TEMPORARILY_DISABLE_LISTING
            GeocacheLogType.Unknown -> GeocachingLog.CACHE_LOG_TYPE_UNKNOWN
            GeocacheLogType.UpdateCoordinates -> GeocachingLog.CACHE_LOG_TYPE_UPDATE_COORDINATES
            GeocacheLogType.WebcamPhotoTaken -> GeocachingLog.CACHE_LOG_TYPE_WEBCAM_PHOTO_TAKEN
            GeocacheLogType.WillAttend -> GeocachingLog.CACHE_LOG_TYPE_WILL_ATTEND
            GeocacheLogType.WriteNote -> GeocachingLog.CACHE_LOG_TYPE_WRITE_NOTE
            GeocacheLogType.Archive -> GeocachingLog.CACHE_LOG_TYPE_ARCHIVE
            GeocacheLogType.Unarchive -> GeocachingLog.CACHE_LOG_TYPE_UNARCHIVE
            else -> GeocachingLog.CACHE_LOG_TYPE_UNKNOWN
        }
    }

    private fun sortLocusGeocachingLogsByDate(waypoint: Waypoint) {
        if (waypoint.gcData?.logs?.isEmpty() != false)
            return

        // Note: Long.compareTo was introduced in API 19
        waypoint.gcData.logs.sortWith(Comparator { lhs, rhs -> if (lhs.date < rhs.date) 1 else if (lhs.date == rhs.date) 0 else -1 })
    }

}
