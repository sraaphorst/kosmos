package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests

/**
 * Tests that `scalarEmbedding(RealField): RingMonomorphism<Real, SplitComplex<Real>>`
 * is a unital ring homomorphism. Sends `r ↦ r + 0j`.
 */
class ScalarEmbeddingSpec : FunSpec({
    lawTests(lawGraph("ℝ ↪ split-complex") {
        val embedding = SplitComplexAlgebras.scalarEmbedding(RealAlgebras.RealField)

        branch("scalarEmbedding(Real) satisfies RingHomomorphismLaws") {
            suite(RingHomomorphismLaws(
                hom = embedding::invoke,
                domain = RealAlgebras.RealField,
                codomain = SplitComplexAlgebras.RealSplitComplexRing,
                arb = ArbReal.fieldReal,
                eqB = SplitComplexAlgebras.eqSplitComplex,
                prA = RealAlgebras.printableRealPretty,
                prB = SplitComplexAlgebras.printableSplitComplexPretty
            ))
        }
    })
})
