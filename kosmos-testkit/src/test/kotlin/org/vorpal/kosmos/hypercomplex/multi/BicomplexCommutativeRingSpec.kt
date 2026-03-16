package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws

class BicomplexCommutativeRingSpec : StringSpec({
    val ring = BicomplexAlgebras.BicomplexCommutativeRing
    val eqB = BicomplexAlgebras.eqBicomplex
    val prB = BicomplexAlgebras.printableBicomplex

    val arbReal = ArbReal.smallReal

    val arbBicomplex =
        Arb.bind(arbReal, arbReal, arbReal, arbReal) { a, b, c, d ->
            Bicomplex.ofStandard(a, b, c, d)
        }

    "BicomplexCommutativeRing satisfies CommutativeRingLaws" {
        CommutativeRingLaws(
            ring = ring,
            arb = arbBicomplex,
            eq = eqB,
            pr = prB
        ).fullTest().throwIfFailed()
    }

    "reciprocal exists for an invertible bicomplex number" {
        val z = Bicomplex.ofIdempotent(
            complex(2.0, 1.0),
            complex(3.0, -4.0)
        )

        check(ring.hasReciprocal(z))
        val inv = ring.reciprocal(z)

        check(eqB(ring.mul(z, inv), ring.one))
        check(eqB(ring.mul(inv, z), ring.one))
    }

    "reciprocal fails for a nonzero zero divisor" {
        val z = Bicomplex.ofIdempotent(
            complex(0.0, 0.0),
            complex(1.0, 2.0)
        )

        check(!ring.hasReciprocal(z))

        try {
            ring.reciprocal(z)
            error("Expected ArithmeticException for reciprocal of non-invertible bicomplex number")
        } catch (_: ArithmeticException) {
            // expected
        }
    }

    "reciprocal fails for zero" {
        val z = ring.zero

        check(!ring.hasReciprocal(z))

        try {
            ring.reciprocal(z)
            error("Expected ArithmeticException for reciprocal of zero")
        } catch (_: ArithmeticException) {
            // expected
        }
    }

    "complex embedding lands on diagonal idempotent coordinates" {
        val z = complex(2.5, -1.25)
        val embedded = BicomplexAlgebras.complexToBicomplexMonomorphism(z)

        check(ComplexAlgebras.eqComplex(embedded.alpha, z))
        check(ComplexAlgebras.eqComplex(embedded.beta, z))
    }

    "first projection returns alpha" {
        val z = Bicomplex.ofIdempotent(
            complex(1.0, 2.0),
            complex(3.0, 4.0)
        )

        val projected = BicomplexAlgebras.BicomplexFirstProjectionHomomorphism(z)

        check(ComplexAlgebras.eqComplex(projected, z.alpha))
    }

    "second projection returns beta" {
        val z = Bicomplex.ofIdempotent(
            complex(1.0, 2.0),
            complex(3.0, 4.0)
        )

        val projected = BicomplexAlgebras.BicomplexSecondProjectionHomomorphism(z)

        check(ComplexAlgebras.eqComplex(projected, z.beta))
    }
})