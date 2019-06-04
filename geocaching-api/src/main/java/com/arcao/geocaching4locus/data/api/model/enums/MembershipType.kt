package com.arcao.geocaching4locus.data.api.model.enums

enum class MembershipType(override val id: Int, override val value: String) : IdValueType {
    UNKNOWN(0, "Unknown"),
    BASIC(1, "Basic"),
    CHARTER(2, "Charter"),
    PREMIUM(3, "Premium");

    companion object {
        fun from(id: Int?) = values().find { it.id == id } ?: UNKNOWN
    }
}