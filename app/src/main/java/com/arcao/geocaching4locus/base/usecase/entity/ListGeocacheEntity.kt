package com.arcao.geocaching4locus.base.usecase.entity

import android.os.Parcelable
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ListGeocacheEntity(
    val referenceCode: String,
    val title: String,
    val geocacheType: Int
) : Parcelable {
    @IgnoredOnParcel
    val id by lazy {
        ReferenceCode.toId(referenceCode)
    }
}