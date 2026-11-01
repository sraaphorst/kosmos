package org.vorpal.kosmos.linear.values

import org.vorpal.kosmos.algebra.structures.CommutativeRng

/**
 * An interface to an object that behaves like a vector, i.e.:
 * - it has a [size]
 * - it has an accessor to get an entry in a given position
 */
interface VecLike<out A : Any> {
    val size: Int
    operator fun get(i: Int): A
}

/**
 * Calculate the dot product of [v] and [w] in the context of [rng].
 *
 * Note that the size of [v] and [w] must be the same.
 */
fun <R : Any> dot(
    v: VecLike<R>,
    w: VecLike<R>,
    rng: CommutativeRng<R>): R {
    if (v.size != w.size)
        throw ArithmeticException("Cannot take dot product of vectors of different lengths: ${v.size} ≠ ${w.size}")
    return (0 until v.size).fold(rng.zero) { sum, idx ->
        rng.run { add(sum, mul(v[idx], w[idx])) }
    }
}
