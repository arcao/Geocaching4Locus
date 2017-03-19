package com.arcao.geocaching4locus.import_gc.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.arcao.geocaching.api.data.Geocache;

public class ParcelableGeocache implements Parcelable {
    private final Geocache geocache;

    ParcelableGeocache(Parcel in) {
        geocache = (Geocache) in.readSerializable();
    }

    public ParcelableGeocache(@NonNull Geocache geocache) {
        this.geocache = geocache;
    }


    @NonNull
    public Geocache get() {
        return geocache;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(geocache);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ParcelableGeocache> CREATOR = new Creator<ParcelableGeocache>() {
        @Override
        public ParcelableGeocache createFromParcel(Parcel in) {
            return new ParcelableGeocache(in);
        }

        @Override
        public ParcelableGeocache[] newArray(int size) {
            return new ParcelableGeocache[size];
        }
    };
}
