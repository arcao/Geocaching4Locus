package com.arcao.geocaching4locus.authentication.util

import android.os.Parcelable
import com.arcao.geocaching.api.data.coordinates.Coordinates
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Account(
    val name: String,
    val avatarUrl: String?,
    val premium: Boolean,
    val homeCoordinates: Coordinates?
) : Parcelable