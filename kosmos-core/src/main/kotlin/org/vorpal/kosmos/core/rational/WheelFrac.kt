package org.vorpal.kosmos.core.rational

import org.vorpal.kosmos.algebra.structures.CommutativeSemiring
import org.vorpal.kosmos.core.Eq

/**
 * A wheel fraction n/d over a base type A.
 *
 * Conventions for Carlström wheels:
 * - bottom/nullity is represented by (0,0)
 * - infinity is represented by (1,0)
 *
 * Note: unless a [FracNormalizer] is used, this is not a canonical representation.
 */
data class WheelFrac<A : Any>(
    val n: A,
    val d: A
)

/**
 * Equality for wheel fractions over a base type A.
 *
 * This implementation uses the conventions of Carlström wheels:
 * - bottom/nullity is represented by (0,0)
 * - infinity is represented by (1,0)
 *
 * The equality is defined as follows:
 * - bottom only equals bottom
 * - infinity equals infinity
 * - finite fractions are compared by cross multiplication
 */
fun <A : Any> wheelFracEq(
    base: CommutativeSemiring<A>,
    eqA: Eq<A>
): Eq<WheelFrac<A>> {
    val zero = base.add.identity

    fun isZero(x: A): Boolean =
        eqA(x, zero)

    fun isBottom(x: WheelFrac<A>): Boolean =
        isZero(x.n) && isZero(x.d)

    fun isInf(x: WheelFrac<A>): Boolean =
        !isZero(x.n) && isZero(x.d)

    return Eq { x, y ->
        // bottom only equals bottom
        if (isBottom(x) || isBottom(y)) return@Eq isBottom(x) && isBottom(y)

        // infinity equals infinity (single infinity model)
        if (isInf(x) || isInf(y)) return@Eq isInf(x) && isInf(y)

        // finite: cross multiply
        val left = base.mul(x.n, y.d)
        val right = base.mul(y.n, x.d)
        eqA(left, right)
    }
}