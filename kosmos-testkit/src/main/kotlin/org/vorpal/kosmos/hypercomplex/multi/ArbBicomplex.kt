package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.hypercomplex.complex.ArbComplex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras

object ArbBicomplex {

    private val complexField = ComplexAlgebras.ComplexField
    private val complexStar = ComplexAlgebras.ComplexStarAlgebra

    /**
     * Generic bicomplex numbers with small real coefficients, drawn in the standard basis
     * `a + bi + cj + dk`.
     */
    val bicomplex: Arb<RealBicomplex> =
        Arb.bind(
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal
        ) { a, b, c, d ->
            Bicomplex.ofStandard(a, b, c, d)
        }

    /**
     * Units of `𝔹`, safely away from the null cone: both idempotent coordinates `𝛂`, `𝜷`
     * have squared modulus in `[0.01, 10000]`, so `z⁻¹` is numerically well-conditioned.
     */
    val unitBicomplex: Arb<RealBicomplex> =
        bicomplex.filter { z ->
            listOf(z.alpha, z.beta).all { c -> complexStar.normSq(c) in 0.01..10_000.0 }
        }

    /**
     * Nonzero zero divisors: exactly one idempotent coordinate vanishes. These are the nonzero
     * elements of the null cone, i.e. the non-units other than `0`.
     *
     * The nonzero coordinate is drawn from [ArbComplex.boundedComplex] (components bounded away
     * from 0), not merely a nonzero complex number: [Bicomplex.ofIdempotent] halves its inputs,
     * and halving a subnormal edge case such as `Double.MIN_VALUE` underflows to `0.0`, which
     * would silently produce the zero bicomplex number instead of a zero divisor.
     */
    val zeroDivisorBicomplex: Arb<RealBicomplex> =
        Arb.choice(
            ArbComplex.boundedComplex.map { alpha -> Bicomplex.ofIdempotent(alpha, complexField.zero) },
            ArbComplex.boundedComplex.map { beta -> Bicomplex.ofIdempotent(complexField.zero, beta) }
        )
}
