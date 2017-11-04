package locus.api.mapper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.arcao.geocaching.api.data.GeocacheLog;
import com.arcao.geocaching.api.data.ImageData;
import com.arcao.geocaching.api.data.User;
import com.arcao.geocaching.api.data.type.GeocacheLogType;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingLog;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import static locus.api.mapper.Util.safeDateLong;

final class GeocacheLogConverter {
  private final ImageDataConverter imageDataConverter;

  GeocacheLogConverter(ImageDataConverter imageDataConverter) {
    this.imageDataConverter = imageDataConverter;
  }

  void addGeocacheLogs(@NonNull Waypoint waypoint, @Nullable Collection<GeocacheLog> logs) {
    if (waypoint.gcData == null || CollectionUtils.isEmpty(logs))
      return;

    for (GeocacheLog log : logs) {
      CollectionUtils.addIgnoreNull(waypoint.gcData.logs, createLocusGeocachingLog(log));
    }

    sortLocusGeocachingLogsByDate(waypoint);
  }

  @Nullable
  private GeocachingLog createLocusGeocachingLog(@Nullable GeocacheLog log) {
    if (log == null)
      return null;

    GeocachingLog l = new GeocachingLog();
    l.setId(log.id());
    l.setDate(safeDateLong(log.visited()));

    User author = log.author();
    if (author != null) {
      l.setFinder(author.userName());
      l.setFindersFound(author.findCount());
      l.setFindersId(author.id());
    }

    l.setLogText(log.text());
    l.setType(createLocusCacheLogType(log.logType()));

    for (ImageData image: log.images()) {
      l.addImage(imageDataConverter.createLocusGeocachingImage(image));
    }

    if (log.updatedCoordinates() != null) {
      l.setCooLat(log.updatedCoordinates().latitude());
      l.setCooLon(log.updatedCoordinates().longitude());
    }

    return l;
  }


  private int createLocusCacheLogType(@Nullable GeocacheLogType logType) {
    if (logType == null)
      return  GeocachingLog.CACHE_LOG_TYPE_UNKNOWN;

    switch (logType) {
      case Announcement:
        return GeocachingLog.CACHE_LOG_TYPE_ANNOUNCEMENT;
      case Attended:
        return GeocachingLog.CACHE_LOG_TYPE_ATTENDED;
      case DidntFindIt:
        return GeocachingLog.CACHE_LOG_TYPE_NOT_FOUND;
      case EnableListing:
        return GeocachingLog.CACHE_LOG_TYPE_ENABLE_LISTING;
      case FoundIt:
        return GeocachingLog.CACHE_LOG_TYPE_FOUND;
      case NeedsArchived:
        return GeocachingLog.CACHE_LOG_TYPE_NEEDS_ARCHIVED;
      case NeedsMaintenance:
        return GeocachingLog.CACHE_LOG_TYPE_NEEDS_MAINTENANCE;
      case OwnerMaintenance:
        return GeocachingLog.CACHE_LOG_TYPE_OWNER_MAINTENANCE;
      case PostReviewerNote:
        return GeocachingLog.CACHE_LOG_TYPE_POST_REVIEWER_NOTE;
      case PublishListing:
        return GeocachingLog.CACHE_LOG_TYPE_PUBLISH_LISTING;
      case RetractListing:
        return GeocachingLog.CACHE_LOG_TYPE_RETRACT_LISTING;
      case TemporarilyDisableListing:
        return GeocachingLog.CACHE_LOG_TYPE_TEMPORARILY_DISABLE_LISTING;
      case Unknown:
        return GeocachingLog.CACHE_LOG_TYPE_UNKNOWN;
      case UpdateCoordinates:
        return GeocachingLog.CACHE_LOG_TYPE_UPDATE_COORDINATES;
      case WebcamPhotoTaken:
        return GeocachingLog.CACHE_LOG_TYPE_WEBCAM_PHOTO_TAKEN;
      case WillAttend:
        return GeocachingLog.CACHE_LOG_TYPE_WILL_ATTEND;
      case WriteNote:
        return GeocachingLog.CACHE_LOG_TYPE_WRITE_NOTE;
      case Archive:
        return GeocachingLog.CACHE_LOG_TYPE_ARCHIVE;
      case Unarchive:
        return GeocachingLog.CACHE_LOG_TYPE_UNARCHIVE;
      default:
        return GeocachingLog.CACHE_LOG_TYPE_UNKNOWN;
    }
  }

  private void sortLocusGeocachingLogsByDate(@NotNull Waypoint waypoint) {
    if (waypoint.gcData == null || CollectionUtils.isEmpty(waypoint.gcData.logs))
      return;

    Collections.sort(waypoint.gcData.logs, new Comparator<GeocachingLog>() {
      @Override
      public int compare(GeocachingLog lhs, GeocachingLog rhs) {
        return lhs.getDate() > rhs.getDate() ? 1 : lhs.getDate() == rhs.getDate() ? -1 : 0;
      }
    });
  }

}
