package org.vorpal.kosmos.linear.values

/**
 * An interface to an object that behaves like a vector, i.e.:
 * - it has a [size]
 * - it has an accessor to get an entry in a given position
 */
interface VecLike<out A : Any> {
    val size: Int
    operator fun get(i: Int): A
}
