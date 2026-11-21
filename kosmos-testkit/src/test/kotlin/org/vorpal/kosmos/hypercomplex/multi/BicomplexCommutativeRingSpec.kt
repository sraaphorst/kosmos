package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

class BicomplexCommutativeRingSpec : FunSpec({
    lawTests(lawGraph("bicomplex ring") {
        val ring = BicomplexAlgebras.BicomplexCommutativeRing
        val eqB = BicomplexAlgebras.eqBicomplex
        val prB = BicomplexAlgebras.printableBicomplex

        branch("BicomplexCommutativeRing satisfies CommutativeRingLaws") {
            suite(CommutativeRingLaws(
                ring = ring,
                arb = ArbBicomplex.bicomplex,
                eq = eqB,
                pr = prB
            ))
        }

        local("reciprocal exists for an invertible bicomplex number") {
            val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField,
                complex(2.0, 1.0),
                complex(3.0, -4.0)
            )

            lawCheck(ring.hasReciprocal(z))
            val inv = ring.reciprocal(z)

            lawCheck(eqB(ring.mul(z, inv), ring.one))
            lawCheck(eqB(ring.mul(inv, z), ring.one))
        }

        local("reciprocal fails for a nonzero zero divisor") {
            val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField,
                complex(0.0, 0.0),
                complex(1.0, 2.0)
            )

            lawCheck(!ring.hasReciprocal(z))

            try {
                ring.reciprocal(z)
                error("Expected ArithmeticException for reciprocal of non-invertible bicomplex number")
            } catch (_: ArithmeticException) {
                // expected
            }
        }

        local("reciprocal fails for zero") {
            val z = ring.zero

            lawCheck(!ring.hasReciprocal(z))

            try {
                ring.reciprocal(z)
                error("Expected ArithmeticException for reciprocal of zero")
            } catch (_: ArithmeticException) {
                // expected
            }
        }

        local("complex embedding lands on diagonal idempotent coordinates") {
            val z = complex(2.5, -1.25)
            val embedded = BicomplexAlgebras.complexToBicomplexMonomorphism(z)

            lawCheck(ComplexAlgebras.eqComplex(embedded.alpha, z))
            lawCheck(ComplexAlgebras.eqComplex(embedded.beta, z))
        }

        local("first projection returns alpha") {
            val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField,
                complex(1.0, 2.0),
                complex(3.0, 4.0)
            )

            val projected = BicomplexAlgebras.BicomplexFirstProjectionHomomorphism(z)

            lawCheck(ComplexAlgebras.eqComplex(projected, z.alpha))
        }

        local("second projection returns beta") {
            val z = BicomplexAlgebras.ofIdempotent(RealAlgebras.RealField,
                complex(1.0, 2.0),
                complex(3.0, 4.0)
            )

            val projected = BicomplexAlgebras.BicomplexSecondProjectionHomomorphism(z)

            lawCheck(ComplexAlgebras.eqComplex(projected, z.beta))
        }
    })
})