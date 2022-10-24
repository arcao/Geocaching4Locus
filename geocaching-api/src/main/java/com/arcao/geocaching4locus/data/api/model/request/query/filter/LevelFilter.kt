package com.arcao.geocaching4locus.data.api.model.request.query.filter

import com.arcao.geocaching4locus.data.api.model.enums.MembershipType
import java.util.Locale

class LevelFilter(private val membershipType: MembershipType) : Filter {
    override fun isValid() =
        membershipType == MembershipType.BASIC || membershipType == MembershipType.PREMIUM

    override fun toString() = "lvl:${membershipType.value.lowercase(Locale.ROOT)}"
}
