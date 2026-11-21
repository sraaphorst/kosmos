package org.vorpal.kosmos.hypercomplex.multi

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.testing.laws.lawTests

/**
 * Property tests for the two ring monomorphisms `𝔹 ↪ M₂(ℂ)`:
 *  - [BicomplexAlgebras.BicomplexToComplexMatrixMonomorphism] (canonical basis), and
 *  - [BicomplexAlgebras.BicomplexToDiagonalMatrixMonomorphism] (orthogonal-idempotent basis).
 *
 * For each we exercise the full unital ring-homomorphism law suite (preserves +, ×, the additive
 * and multiplicative identities) together with injectivity — the property that makes them
 * *mono*morphisms in the first place.
 */
class BicomplexMatrixMonomorphismSpec : FunSpec({
    lawTests(lawGraph("bicomplex ↪ M₂(ℂ)") {
        val canonical = BicomplexAlgebras.BicomplexToComplexMatrixMonomorphism
        val diagonal = BicomplexAlgebras.BicomplexToDiagonalMatrixMonomorphism

        val eqB = BicomplexAlgebras.eqBicomplex
        val prB = BicomplexAlgebras.printableBicomplexPretty
        val eqM = DenseMatAlgebras.liftEq(ComplexAlgebras.eqComplex)
        val prM = DenseMatAlgebras.liftPrintablePretty(ComplexAlgebras.printableComplexPretty)

        branch("BicomplexToComplexMatrixMonomorphism (canonical basis) satisfies UnitalRingHomomorphismLaws") {
            suite(RingHomomorphismLaws(
                hom = canonical::invoke,
                domain = canonical.domain,
                codomain = canonical.codomain,
                arb = ArbBicomplex.bicomplex,
                eqB = eqM,
                prA = prB,
                prB = prM
            ))
        }

        law("BicomplexToComplexMatrixMonomorphism (canonical basis) is injective", injectivityLaw(
                hom = canonical::invoke,
                arbA = ArbBicomplex.bicomplex,
                eqA = eqB,
                eqB = eqM,
                prA = prB,
                prB = prM
            ))

        branch("BicomplexToDiagonalMatrixMonomorphism (idempotent basis) satisfies UnitalRingHomomorphismLaws") {
            suite(RingHomomorphismLaws(
                hom = diagonal::invoke,
                domain = diagonal.domain,
                codomain = diagonal.codomain,
                arb = ArbBicomplex.bicomplex,
                eqB = eqM,
                prA = prB,
                prB = prM
            ))
        }

        law("BicomplexToDiagonalMatrixMonomorphism (idempotent basis) is injective", injectivityLaw(
                hom = diagonal::invoke,
                arbA = ArbBicomplex.bicomplex,
                eqA = eqB,
                eqB = eqM,
                prA = prB,
                prB = prM
            ))
    })
})
