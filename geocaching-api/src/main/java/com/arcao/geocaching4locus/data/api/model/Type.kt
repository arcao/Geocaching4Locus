package com.arcao.geocaching4locus.data.api.model

abstract class Type(
    /** identifier of the type **/
    val id: Int,
    /** the name of the type **/
    val name: String,
    /** link to the image of the type **/
    val imageUrl: String
)
