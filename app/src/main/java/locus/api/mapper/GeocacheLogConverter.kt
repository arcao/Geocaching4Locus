package locus.api.mapper

import com.arcao.geocaching4locus.data.api.model.GeocacheLog
import com.arcao.geocaching4locus.data.api.model.GeocacheLogType
import locus.api.objects.geoData.Point
import locus.api.objects.geocaching.GeocachingLog
import locus.api.utils.addIgnoreNull

class GeocacheLogConverter(
    private val imageDataConverter: ImageDataConverter
) {

    fun addGeocacheLogs(point: Point, logs: Collection<GeocacheLog>) {
        if (point.gcData?.logs == null || logs.isEmpty())
            return

        for (log in logs) {
            point.gcData?.logs?.addIgnoreNull(createLocusGeocachingLog(log))
        }

        sortLocusGeocachingLogsByDate(point)
    }

    fun createLocusGeocachingLogs(logs: Collection<GeocacheLog>): Collection<GeocachingLog> {
        return logs.map(this::createLocusGeocachingLog)
    }

    private fun createLocusGeocachingLog(log: GeocacheLog): GeocachingLog {
        return GeocachingLog().apply {
            id = log.id
            date = log.loggedDateInstant?.toEpochMilli() ?: 0
            logText = log.text.orEmpty()
            type = log.geocacheLogType.toLocusMapLogType()

            val author = log.owner
            if (author != null) {
                finder = author.username.orEmpty()
                findersFound = author.findCount
                findersId = author.id
            }

            for (image in log.images ?: emptyList()) {
                imageDataConverter.createLocusGeocachingImage(image)?.let(this::addImage)
            }
            log.updatedCoordinates?.let { coordinates ->
                cooLat = coordinates.latitude
                cooLon = coordinates.longitude
            }
        }
    }

    private fun GeocacheLogType?.toLocusMapLogType(): Int {
        return when (this?.id ?: GeocacheLogType.WRITE_NOTE) {
            GeocacheLogType.EVENT_ANNOUNCEMENT -> GeocachingLog.CACHE_LOG_TYPE_ANNOUNCEMENT
            GeocacheLogType.ATTENDED -> GeocachingLog.CACHE_LOG_TYPE_ATTENDED
            GeocacheLogType.DNF_IT -> GeocachingLog.CACHE_LOG_TYPE_NOT_FOUND
            GeocacheLogType.ENABLE_LISTING -> GeocachingLog.CACHE_LOG_TYPE_ENABLE_LISTING
            GeocacheLogType.FOUND_IT -> GeocachingLog.CACHE_LOG_TYPE_FOUND
            GeocacheLogType.NEEDS_ARCHIVING -> GeocachingLog.CACHE_LOG_TYPE_NEEDS_ARCHIVED
            GeocacheLogType.NEEDS_MAINTENANCE -> GeocachingLog.CACHE_LOG_TYPE_NEEDS_MAINTENANCE
            GeocacheLogType.OWNER_MAINTENANCE -> GeocachingLog.CACHE_LOG_TYPE_OWNER_MAINTENANCE
            GeocacheLogType.POST_REVIEWER_NOTE -> GeocachingLog.CACHE_LOG_TYPE_POST_REVIEWER_NOTE
            GeocacheLogType.PUBLISH_LISTING -> GeocachingLog.CACHE_LOG_TYPE_PUBLISH_LISTING
            GeocacheLogType.RETRACT_LISTING -> GeocachingLog.CACHE_LOG_TYPE_RETRACT_LISTING
            GeocacheLogType.TEMPORARILY_DISABLE_LISTING -> GeocachingLog.CACHE_LOG_TYPE_TEMPORARILY_DISABLE_LISTING
            GeocacheLogType.UPDATE_COORDINATES -> GeocachingLog.CACHE_LOG_TYPE_UPDATE_COORDINATES
            GeocacheLogType.WEBCAM_PHOTO_TAKEN -> GeocachingLog.CACHE_LOG_TYPE_WEBCAM_PHOTO_TAKEN
            GeocacheLogType.WILL_ATTEND -> GeocachingLog.CACHE_LOG_TYPE_WILL_ATTEND
            GeocacheLogType.WRITE_NOTE -> GeocachingLog.CACHE_LOG_TYPE_WRITE_NOTE
            GeocacheLogType.ARCHIVE -> GeocachingLog.CACHE_LOG_TYPE_ARCHIVE
            GeocacheLogType.UNARCHIVE -> GeocachingLog.CACHE_LOG_TYPE_UNARCHIVE
            GeocacheLogType.SUBMIT_FOR_REVIEW -> GeocachingLog.CACHE_LOG_TYPE_WRITE_NOTE
            else -> GeocachingLog.CACHE_LOG_TYPE_WRITE_NOTE
        }
    }

    private fun sortLocusGeocachingLogsByDate(waypoint: Point) {
        if (waypoint.gcData?.logs?.isEmpty() != false)
            return

        waypoint.gcData?.logs?.sortWith { lhs, rhs -> lhs.date.compareTo(rhs.date) }
    }
}
