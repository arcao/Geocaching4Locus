package com.arcao.auto.value.parcel

import android.os.Parcel

import com.arcao.geocaching.api.data.coordinates.Coordinates
import com.ryanharter.auto.value.parcel.TypeAdapter

class CoordinatesTypeAdapter : TypeAdapter<Coordinates> {
    override fun fromParcel(src: Parcel): Coordinates =
            Coordinates.create(src.readDouble(), src.readDouble())

    override fun toParcel(value: Coordinates, dest: Parcel) {
        with(dest) {
            writeDouble(value.latitude())
            writeDouble(value.longitude())
        }
    }
}
