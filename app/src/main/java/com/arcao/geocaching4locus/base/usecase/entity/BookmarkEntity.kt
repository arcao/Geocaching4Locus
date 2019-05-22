package com.arcao.geocaching4locus.base.usecase.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookmarkEntity(
    val code: String,
    val title: String,
    val geocacheType: Int
) : Parcelable