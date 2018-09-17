package locus.api.mapper;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.GeocacheLog;
import com.arcao.geocaching.api.data.Trackable;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import locus.api.objects.extra.Waypoint;

final public class DataMapper {
    private final GeocacheConverter geocacheConverter;
    private final GeocacheLogConverter geocacheLogConverter;
    private final TrackableConverter trackableConverter;

    public DataMapper(@NonNull Context context) {
        geocacheConverter = new GeocacheConverter(context);
        geocacheLogConverter = geocacheConverter.getGeocacheLogConverter();
        trackableConverter = geocacheConverter.getTrackableConverter();
    }

    @NonNull
    public List<Waypoint> createLocusWaypoints(@Nullable Collection<Geocache> geocaches) {
        if (CollectionUtils.isEmpty(geocaches))
            return Collections.emptyList();

        List<Waypoint> points = new ArrayList<>(geocaches.size());
        for (Geocache cache : geocaches) {
            CollectionUtils.addIgnoreNull(points, createLocusWaypoint(cache));
        }

        return points;
    }

    @Nullable
    public Waypoint createLocusWaypoint(@NonNull Geocache geocache) {
        return geocacheConverter.createLocusWaypoint(geocache);
    }

    public void addCacheLogs(@NonNull Waypoint waypoint, @Nullable Collection<GeocacheLog> logs) {
        geocacheLogConverter.addGeocacheLogs(waypoint, logs);
    }

    public void addTrackables(@NonNull Waypoint waypoint, @Nullable Collection<Trackable> trackables) {
        trackableConverter.addTrackables(waypoint, trackables);
    }
}
