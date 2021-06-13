package com.arcao.geocaching4locus.base.usecase.entity

import android.os.Parcelable
import com.arcao.geocaching4locus.data.api.util.ReferenceCode
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

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