package org.vorpal.kosmos.algebra.extensions

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.math.Real
import kotlin.math.abs

/**
 * Arbitrary for finite, non-NaN Reals suitable for dual number operations.
 */
fun arbDualReal(): Arb<Real> =
    Arb.double(-100.0, 100.0)
        .filter { it.isFinite() && !it.isNaN() }

/**
 * Arbitrary for non-zero Reals (for testing invertibility).
 */
fun arbNonZeroDualReal(): Arb<Real> =
    arbDualReal().filter { abs(it) > 1e-6 }

/**
 * Arbitrary for dual numbers over Real field.
 */
fun arbDual(): Arb<DualRing<Real>.Dual> = arbitrary {
    val ring = RealField.dual()
    val a = arbDualReal().bind()
    val b = arbDualReal().bind()
    ring.Dual(a, b)
}

/**
 * Arbitrary for invertible dual numbers (a ≠ 0).
 */
fun arbInvertibleDual(): Arb<DualRing<Real>.Dual> = arbitrary {
    val ring = RealField.dual()
    val a = arbNonZeroDualReal().bind()
    val b = arbDualReal().bind()
    ring.Dual(a, b)
}

/**
 * Arbitrary for non-invertible dual numbers (a = 0).
 */
fun arbNonInvertibleDual(): Arb<DualRing<Real>.Dual> = arbitrary {
    val ring = RealField.dual()
    val b = arbNonZeroDualReal().bind()
    ring.Dual(0.0, b)
}
