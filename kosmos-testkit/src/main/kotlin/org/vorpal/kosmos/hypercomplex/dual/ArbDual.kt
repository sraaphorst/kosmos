package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import kotlin.math.abs

/**
 * Arbitrary for finite Reals suitable for dual number operations.
 */
val arbDualReal: Arb<Real> =
    Arb.double(-100.0, 100.0)
        .filter { it.isFinite() }

/**
 * Arbitrary for non-zero Reals, using approximate real equality.
 */
val arbNonZeroDualReal: Arb<Real> =
    arbDualReal
        .filter { Eqs.realApprox().neqv(it, 0.0) }

/**
 * Generate a Real that is safely invertible for dual number operations.
 *
 * This avoids reciprocal tests becoming numerically noisy near zero.
 */
private val arbSafelyInvertibleDualReal: Arb<Real> =
    arbDualReal
        .filter { abs(it) > 1e-4 }

/**
 * Arbitrary for dual numbers over Real.
 */
val arbDual: Arb<Dual<Real>> =
    arbitrary {
        dual(
            f = arbDualReal.bind(),
            df = arbDualReal.bind()
        )
    }

/**
 * Arbitrary for invertible dual numbers.
 *
 * The real part is non-zero according to approximate real equality.
 */
val arbInvertibleDual: Arb<Dual<Real>> =
    arbitrary {
        dual(
            f = arbNonZeroDualReal.bind(),
            df = arbDualReal.bind()
        )
    }

/**
 * Arbitrary for safely invertible dual numbers.
 *
 * The real part is bounded away from zero to keep reciprocal tests stable.
 */
val arbSafelyInvertibleDual: Arb<Dual<Real>> =
    arbitrary {
        dual(
            f = arbSafelyInvertibleDualReal.bind(),
            df = arbDualReal.bind()
        )
    }

/**
 * Arbitrary for non-invertible dual numbers.
 *
 * The real part is zero, and the infinitesimal part is non-zero to avoid
 * always generating the additive zero element.
 */
val arbNonInvertibleDual: Arb<Dual<Real>> =
    arbitrary {
        val b = arbNonZeroDualReal.bind()
        dual(
            f = 0.0,
            df = b
        )
    }

val arbRational: Arb<Rational> =
    Arb.int(-20, 20)
        .map { it.toRational() }

val arbNonZeroRational: Arb<Rational> =
    arbRational
        .filter { it != Rational.ZERO }

val arbDualRational: Arb<Dual<Rational>> =
    arbitrary {
        dual(
            f = arbRational.bind(),
            df = arbRational.bind()
        )
    }

val arbInvertibleDualRational: Arb<Dual<Rational>> =
    arbitrary {
        dual(
            f = arbNonZeroRational.bind(),
            df = arbRational.bind()
        )
    }


fun <V : Any> arbDualVector(arbV: Arb<V>): Arb<DualVector<V>> =
    arbitrary {
        DualVector(
            primal = arbV.bind(),
            tangent = arbV.bind()
        )
    }
