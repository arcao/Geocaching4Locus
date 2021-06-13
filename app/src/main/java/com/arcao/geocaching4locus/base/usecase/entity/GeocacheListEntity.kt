package com.arcao.geocaching4locus.base.usecase.entity

import android.os.Parcelable
import com.arcao.geocaching4locus.data.api.model.enums.GeocacheListType
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeocacheListEntity(
    val id: Long,
    val guid: String,
    val name: String,
    val description: String?,
    val itemCount: Int,
    val shared: Boolean,
    val publicList: Boolean,
    val type: GeocacheListType
) : Parcelable