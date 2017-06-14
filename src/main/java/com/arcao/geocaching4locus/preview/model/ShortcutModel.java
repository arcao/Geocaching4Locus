package com.arcao.geocaching4locus.preview.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ShortcutModel {
    public abstract Intent intent();

    public abstract Drawable icon();

    public abstract CharSequence title();

    public static Builder builder() {
        return new AutoValue_ShortcutModel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder intent(Intent intent);

        public abstract Builder icon(Drawable icon);

        public abstract Builder title(CharSequence title);

        public abstract ShortcutModel build();
    }
}
