package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.homomorphism.injectivityLaw
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.testing.laws.lawTests

/**
 * Mirrors `ComplexToRank2RMatrixMonomorphismSpec`.
 *
 * Tests that the canonical embedding
 * ```text
 * a + bj ↦ [[a, b], [b, a]]
 * ```
 * from `SplitComplex<Real>` into `M₂(Real)` is a unital ring homomorphism.
 */
class SplitComplexToRank2RMatrixMonomorphismSpec : FunSpec({
    lawTests(lawGraph("split-complex ↪ M₂(ℝ)") {
        val mono = SplitComplexAlgebras.splitComplexToRank2MatrixMonomorphism(
            RealAlgebras.RealField
        )
        val eqA = SplitComplexAlgebras.eqSplitComplex
        val prA = SplitComplexAlgebras.printableSplitComplexPretty
        val eqM = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox)
        val prM = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)

        branch("splitComplexToRank2MatrixMonomorphism satisfies UnitalRingHomomorphismLaws") {
            suite(RingHomomorphismLaws(
                hom = mono::invoke,
                domain = SplitComplexAlgebras.RealSplitComplexRing,
                codomain = DenseMatAlgebras.DenseMatRing(RealAlgebras.RealField, 2),
                arb = ArbSplitComplex.boundedSplitComplex,
                eqB = eqM,
                prA = prA,
                prB = prM
            ))
        }

        law("splitComplexToRank2MatrixMonomorphism is injective", injectivityLaw(
                hom = mono::invoke,
                arbA = ArbSplitComplex.boundedSplitComplex,
                eqA = eqA,
                eqB = eqM,
                prA = prA,
                prB = prM
            ))
    })
})
