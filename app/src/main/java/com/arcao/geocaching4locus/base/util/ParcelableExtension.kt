package com.arcao.geocaching4locus.base.util

import android.os.Parcel
import android.os.Parcelable

fun Parcelable.marshall(): ByteArray {
    val parcel = Parcel.obtain()
    try {
        writeToParcel(parcel, 0)
        return parcel.marshall()
    } finally {
        parcel.recycle()
    }
}

fun <T : Parcelable> ByteArray.unmarshall(creator: Parcelable.Creator<T>): T {
    val parcel = Parcel.obtain()
    try {
        parcel.unmarshall(this, 0, size)
        parcel.setDataPosition(0) // this is extremely important!
        return creator.createFromParcel(parcel)
    } finally {
        parcel.recycle()
    }
}
