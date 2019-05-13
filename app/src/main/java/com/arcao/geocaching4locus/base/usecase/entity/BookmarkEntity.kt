package com.arcao.geocaching4locus.base.usecase.entity

import android.os.Parcelable
import com.arcao.geocaching.api.data.type.GeocacheType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookmarkEntity(
    val code: String,
    val title: String,
    val geocacheType: GeocacheType
) : Parcelable