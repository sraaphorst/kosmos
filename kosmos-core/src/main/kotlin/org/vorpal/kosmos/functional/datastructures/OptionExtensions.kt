package org.vorpal.kosmos.functional.datastructures

/**
 * Easy way to make anything nullable into an [Option].
 * This is particularly useful for parsing heterogeneous JSON-ish blobs.
 */
inline fun <reified T> Any?.asOption(): Option<T> = when (this) {
    is T -> Option.Some(this)
    else -> Option.None
}

/**
 * Unwrap an [Option.Some], throwing an [IllegalStateException] with [message] otherwise.
 *
 * Intended for tests and other contexts where a `None` outcome indicates a programming error
 * rather than a runtime branch to be handled. In production code prefer [getOrElse],
 * [fold], or an explicit pattern match.
 */
fun <A> Option<A>.expectSome(message: String = "Expected Option.Some, but got Option.None"): A =
    when (this) {
        is Option.Some -> value
        Option.None -> error(message)
    }
