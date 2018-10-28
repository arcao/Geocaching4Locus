package locus.api.utils

import java.util.*

inline fun <reified T> MutableList<in T>.addIgnoreNull(item: T?) {
    if (item != null) add(item)
}

inline fun <reified T> Collection<T>?.isNullOrEmpty() = this == null || isEmpty()

@Suppress("NOTHING_TO_INLINE")
inline fun Date?.toTime() = this?.time ?: 0