package org.vorpal.kosmos.algebra.extensions

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import org.vorpal.kosmos.core.math.Real

/**
 * Arbitrary for finite Reals suitable for dual number operations.
 */
fun arbDualReal(): Arb<Real> =
    Arb.double(-100.0, 100.0)
        .filter { it.isFinite() }   // finite excludes NaN and ±Inf

/**
 * Arbitrary for non-zero Reals (exactly non-zero, matching Dual invertibility tests).
 */
fun arbNonZeroDualReal(): Arb<Real> =
    arbDualReal().filter { it != 0.0 }

/**
 * Arbitrary for dual numbers over Real.
 */
fun arbDual(): Arb<Dual<Real>> = arbitrary {
    val a = arbDualReal().bind()
    val b = arbDualReal().bind()
    Dual(a, b)
}

/**
 * Arbitrary for invertible dual numbers (real part a ≠ 0).
 */
fun arbInvertibleDual(): Arb<Dual<Real>> = arbitrary {
    val a = arbNonZeroDualReal().bind()
    val b = arbDualReal().bind()
    Dual(a, b)
}

/**
 * Arbitrary for non-invertible dual numbers (real part a = 0).
 *
 * We choose b ≠ 0 to avoid always producing the additive zero element.
 */
fun arbNonInvertibleDual(): Arb<Dual<Real>> = arbitrary {
    val b = arbNonZeroDualReal().bind()
    Dual(0.0, b)
}
