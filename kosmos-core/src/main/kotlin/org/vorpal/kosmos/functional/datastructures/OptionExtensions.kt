package org.vorpal.kosmos.functional.datastructures

/**
 * Easy way to make anything nullable into an [Option].
 * This is particularly useful for parsing heterogeneous JSON-ish blobs.
 */
inline fun <reified T> Any?.asOption(): Option<T> = when (this) {
    is T -> Option.Some(this)
    else -> Option.None
}
