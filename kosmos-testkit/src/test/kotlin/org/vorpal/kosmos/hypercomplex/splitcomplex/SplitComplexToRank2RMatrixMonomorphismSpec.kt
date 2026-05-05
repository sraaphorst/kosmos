package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras

/**
 * Mirrors `ComplexToRank2RMatrixMonomorphismSpec`.
 *
 * Tests that the canonical embedding
 * ```text
 * a + bj ↦ [[a, b], [b, a]]
 * ```
 * from `SplitComplex<Real>` into `M₂(Real)` is a unital ring homomorphism.
 */
object SplitComplexToRank2RMatrixMonomorphismSpec : StringSpec({
    val mono = SplitComplexAlgebras.splitComplexToRank2MatrixMonomorphism(
        RealAlgebras.RealField
    )

    "splitComplexToRank2MatrixMonomorphism satisfies RingHomomorphismLaws" {
        RingHomomorphismLaws(
            hom = mono::invoke,
            domain = SplitComplexAlgebras.RealSplitComplexRing,
            codomain = DenseMatAlgebras.DenseMatRing(RealAlgebras.RealField, 2),
            arb = ArbSplitComplex.boundedSplitComplex,
            eqB = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox),
            prA = SplitComplexAlgebras.printableSplitComplexPretty,
            prB = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
        ).fullTest().throwIfFailed()
    }
})
