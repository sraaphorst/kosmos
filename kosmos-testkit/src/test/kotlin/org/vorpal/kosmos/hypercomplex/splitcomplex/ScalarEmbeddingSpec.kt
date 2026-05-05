package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws

/**
 * Tests that `scalarEmbedding(RealField): RingMonomorphism<Real, SplitComplex<Real>>`
 * is a unital ring homomorphism. Sends `r ↦ r + 0j`.
 */
object ScalarEmbeddingSpec : StringSpec({
    val embedding = SplitComplexAlgebras.scalarEmbedding(RealAlgebras.RealField)

    "scalarEmbedding(Real) satisfies RingHomomorphismLaws" {
        RingHomomorphismLaws(
            hom = embedding::invoke,
            domain = RealAlgebras.RealField,
            codomain = SplitComplexAlgebras.RealSplitComplexRing,
            arb = ArbReal.fieldReal,
            eqB = SplitComplexAlgebras.eqSplitComplex,
            prA = RealAlgebras.printableRealPretty,
            prB = SplitComplexAlgebras.printableSplitComplexPretty
        ).fullTest().throwIfFailed()
    }
})
