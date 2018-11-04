package com.arcao.geocaching4locus.base.util

import kotlin.reflect.KClass

/**
 * Helper to force a when statement to assert all options are matched in a when statement.
 *
 * By default, Kotlin doesn't care if all branches are handled in a when statement. However, if you
 * use the when statement as an expression (with a value) it will force all cases to be handled.
 *
 * This helper is to make a lightweight way to say you meant to match all of them.
 *
 * Usage:
 *
 * ```
 * when(sealedObject) {
 *     is OneType -> //
 *     is AnotherType -> //
 * }.exhaustive
 */

val <T> T.exhaustive: T
    get() = this

inline fun <reified T : Any> Any?.ifIs(t : KClass<T>, block : T.() -> Unit) {
    if (this is T) {
        block()
    }
}
