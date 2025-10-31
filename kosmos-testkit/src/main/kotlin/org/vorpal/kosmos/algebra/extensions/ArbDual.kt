package org.vorpal.kosmos.algebra.extensions

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import kotlin.math.abs

/**
 * Arbitrary for finite, non-NaN doubles suitable for dual number operations.
 */
fun arbDualDouble(): Arb<Double> =
    Arb.double(-100.0, 100.0)
        .filter { it.isFinite() && !it.isNaN() }

/**
 * Arbitrary for non-zero doubles (for testing invertibility).
 */
fun arbNonZeroDualDouble(): Arb<Double> =
    arbDualDouble().filter { abs(it) > 1e-6 }

/**
 * Arbitrary for dual numbers over Double field.
 */
fun arbDual(): Arb<DualRing<Double>.Dual> = arbitrary {
    val ring = RealField.dual()
    val a = arbDualDouble().bind()
    val b = arbDualDouble().bind()
    ring.Dual(a, b)
}

/**
 * Arbitrary for invertible dual numbers (a ≠ 0).
 */
fun arbInvertibleDual(): Arb<DualRing<Double>.Dual> = arbitrary {
    val ring = RealField.dual()
    val a = arbNonZeroDualDouble().bind()
    val b = arbDualDouble().bind()
    ring.Dual(a, b)
}

/**
 * Arbitrary for non-invertible dual numbers (a = 0).
 */
fun arbNonInvertibleDual(): Arb<DualRing<Double>.Dual> = arbitrary {
    val ring = RealField.dual()
    val b = arbNonZeroDualDouble().bind()
    ring.Dual(0.0, b)
}
