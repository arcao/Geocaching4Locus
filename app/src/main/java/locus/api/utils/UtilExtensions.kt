package locus.api.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

inline fun <reified T> MutableList<in T>.addIgnoreNull(item: T?) {
    if (item != null) add(item)
}

@UseExperimental(ExperimentalContracts::class)
inline fun <reified T> Collection<T>?.isNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@isNullOrEmpty != null)
    }
    return this == null || isEmpty()
}
