package org.vorpal.kosmos.linear.instance

import io.kotest.property.Arb
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.shuffle
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.linear.values.PermMat

/**
 * Equality on [PermMat] by the underlying one-line representation.
 *
 * [PermMat] does not override `equals`, so structural comparison must go through the
 * `IntArray` it wraps. This [Eq] is what law suites over the symmetric group should use.
 */
val eqPermMat: Eq<PermMat> =
    Eq { a, b -> a.toIntArray().contentEquals(b.toIntArray()) }

/**
 * Generate a uniformly random `n×n` permutation matrix (Fisher–Yates over `0 until n`).
 */
fun arbPermMat(n: Int): Arb<PermMat> {
    require(n >= 0) { "n must be nonnegative, got $n" }
    return Arb.shuffle((0 until n).toList()).map { PermMat.of(it.toIntArray()) }
}

/**
 * Generate a random permutation matrix whose order is uniformly chosen in `[minN, maxN]`.
 */
fun arbPermMatOfVaryingSize(minN: Int, maxN: Int): Arb<PermMat> {
    require(minN >= 0) { "minN must be nonnegative, got $minN" }
    require(maxN >= minN) { "maxN must be >= minN, got minN=$minN, maxN=$maxN" }
    return Arb.int(minN..maxN).flatMap { n -> arbPermMat(n) }
}
