package com.arcao.auto.value.parcel;

import android.os.Parcel;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.ryanharter.auto.value.parcel.TypeAdapter;

public class CoordinatesTypeAdapter implements TypeAdapter<Coordinates> {
  public Coordinates fromParcel(Parcel in) {
    return Coordinates.create(in.readDouble(), in.readDouble());
  }

  public void toParcel(Coordinates value, Parcel dest) {
    dest.writeDouble(value.latitude());
    dest.writeDouble(value.longitude());
  }
}
