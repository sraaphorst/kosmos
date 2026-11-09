package org.vorpal.kosmos.linear.values

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind

/**
 * Arbitraries for the fixed-size matrix type [Mat2].
 *
 * The generator is parameterized by the entry [Arb], so the same generator works over any ring
 * (integers, rationals, a prime field, quaternions, and so on).
 */

/** Generate a random [Mat2] with all four entries drawn independently from [arbA]. */
fun <A : Any> arbMat2(arbA: Arb<A>): Arb<Mat2<A>> =
    Arb.bind(arbA, arbA, arbA, arbA) { a, b, c, d -> Mat2(a, b, c, d) }
