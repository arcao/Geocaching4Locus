package com.arcao.geocaching4locus.base.usecase.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BookmarkListEntity(
    val id: Int,
    val guid: String,
    val name: String,
    val description: String?,
    val itemCount: Int,
    val shared: Boolean,
    val publicList: Boolean,
    val archived: Boolean,
    val special: Boolean,
    val type: Int
) : Parcelable