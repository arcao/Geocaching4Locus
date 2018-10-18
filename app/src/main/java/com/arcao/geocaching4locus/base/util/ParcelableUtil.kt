package com.arcao.geocaching4locus.base.util

import android.os.Parcel
import android.os.Parcelable

object ParcelableUtil {
    @JvmStatic
    fun marshall(parcelable: Parcelable): ByteArray {
        val parcel = Parcel.obtain()
        try {
            parcelable.writeToParcel(parcel, 0)
            return parcel.marshall()
        } finally {
            parcel.recycle()
        }
    }

    @JvmStatic
    fun <T> unmarshall(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
        val parcel = Parcel.obtain().apply {
            unmarshall(bytes, 0, bytes.size)
            setDataPosition(0) // this is extremely important!
        }
        try {
            return creator.createFromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Parcelable.marshall() = ParcelableUtil.marshall(this)

@Suppress("NOTHING_TO_INLINE")
inline fun <reified T> ByteArray.unmarshall(creator: Parcelable.Creator<T>) = ParcelableUtil.unmarshall(this, creator)