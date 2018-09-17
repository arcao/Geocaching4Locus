package locus.api.mapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.arcao.geocaching.api.data.Trackable;
import com.arcao.geocaching.api.data.User;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;

import locus.api.objects.extra.Waypoint;
import locus.api.objects.geocaching.GeocachingTrackable;

import static locus.api.mapper.Util.safeDateLong;

final class TrackableConverter {
    void addTrackables(@NonNull Waypoint waypoint, @Nullable Collection<Trackable> trackables) {
        if (waypoint.gcData == null || CollectionUtils.isEmpty(trackables))
            return;

        boolean trackableLightData = trackables.size() > 100;
        for (Trackable trackable : trackables) {
            CollectionUtils.addIgnoreNull(waypoint.gcData.trackables, createLocusGeocachingTrackable(trackable, trackableLightData));
        }
    }

    @Nullable
    private GeocachingTrackable createLocusGeocachingTrackable(@Nullable Trackable trackable, boolean trackableLightData) {
        if (trackable == null)
            return null;

        GeocachingTrackable t = new GeocachingTrackable();
        t.setId(trackable.id());
        t.setImgUrl(trackable.trackableTypeImage());
        t.setName(trackable.name());
        User currentOwner = trackable.currentOwner();
        if (currentOwner != null) {
            t.setCurrentOwner(currentOwner.userName());
        }
        User owner = trackable.owner();
        if (owner != null) {
            t.setOriginalOwner(owner.userName());
        }
        t.setSrcDetails(trackable.trackableUrl());
        t.setReleased(safeDateLong(trackable.created()));

        if (!trackableLightData) {
            t.setDetails(trackable.description());
            t.setGoal(trackable.goal());
        }
        return t;
    }
}
