package locus.api.extension

import locus.api.objects.extra.Location
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun Location?.isInvalid(): Boolean {
    contract {
        returns(false) implies (this@isInvalid is Location)
    }
    return this == null || latitude.isNaN() || longitude.isNaN()
}
