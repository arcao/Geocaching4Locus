package com.arcao.geocaching4locus.authentication.util;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Account implements Parcelable {
    public abstract String name();
    public abstract boolean premium();
    @Nullable
    public abstract String avatarUrl();
    @Nullable
    public abstract Coordinates homeCoordinates();

    public static Builder builder() {
        return new AutoValue_Account.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);
        public abstract Builder premium(boolean premium);
        public abstract Builder avatarUrl(@Nullable String avatarUrl);
        public abstract Builder homeCoordinates(@Nullable Coordinates homeCoordinates);
        public abstract Account build();
    }
}
