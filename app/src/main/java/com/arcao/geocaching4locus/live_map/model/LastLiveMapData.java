package com.arcao.geocaching4locus.live_map.model;

import android.content.Intent;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
import locus.api.android.utils.LocusUtils;
import locus.api.objects.extra.Location;

public class LastLiveMapData {
  private static final String VAR_LOC_MAP_CENTER = "1302";
  private static final String VAR_LOC_MAP_BBOX_TOP_LEFT = "1303";
  private static final String VAR_LOC_MAP_BBOX_BOTTOM_RIGHT = "1304";


  private static final LastLiveMapData INSTANCE = new LastLiveMapData();

  private Coordinates mapCenterCoordinates;
  private Coordinates mapTopLeftCoordinates;
  private Coordinates mapBottomRightCoordinates;


  private LastLiveMapData() {
  }

  public static LastLiveMapData getInstance() {
    return INSTANCE;
  }

  public void update(Intent intent) {
    mapCenterCoordinates = getCoordinatesFromIntent(intent, VAR_LOC_MAP_CENTER);
    mapTopLeftCoordinates = getCoordinatesFromIntent(intent, VAR_LOC_MAP_BBOX_TOP_LEFT);
    mapBottomRightCoordinates = getCoordinatesFromIntent(intent, VAR_LOC_MAP_BBOX_BOTTOM_RIGHT);
  }

  public Coordinates getMapCenterCoordinates() {
    return mapCenterCoordinates;
  }

  public Coordinates getMapTopLeftCoordinates() {
    return mapTopLeftCoordinates;
  }

  public Coordinates getMapBottomRightCoordinates() {
    return mapBottomRightCoordinates;
  }

  public boolean isValid() {
    return mapCenterCoordinates != null && mapTopLeftCoordinates != null && mapBottomRightCoordinates != null;
  }

  public void remove() {
    mapCenterCoordinates = null;
    mapTopLeftCoordinates = null;
    mapBottomRightCoordinates = null;
  }

  private Coordinates getCoordinatesFromIntent(Intent intent, String extraName) {
    Location location = LocusUtils.getLocationFromIntent(intent, extraName);
    if (location == null || Double.isNaN(location.latitude) || Double.isNaN(location.longitude))
      return null;

    return Coordinates.create(location.latitude, location.longitude);
  }
}
