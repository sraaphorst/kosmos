package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws

/**
 * Property tests for the split-complex ring over the Reals,
 * `RealSplitComplexRing`, which is `CommutativeInvolutiveRing<SplitComplex<Real>>`.
 *
 * Mirrors `ComplexFieldSpec`, except that `SplitComplex<Real>` is *not* a field
 * (it has zero divisors on the null cone `a² = b²`), so the strongest abstract
 * laws we can test here are those of a commutative involutive ring.
 */
object RealSplitComplexRingSpec : StringSpec({
    "RealSplitComplexRing satisfies CommutativeRingLaws" {
        CommutativeRingLaws(
            ring = SplitComplexAlgebras.RealSplitComplexRing,
            arb = ArbSplitComplex.boundedSplitComplex,
            eq = SplitComplexAlgebras.eqSplitComplex,
            pr = SplitComplexAlgebras.printableSplitComplexPretty
        ).fullTest().throwIfFailed()
    }

    "RealSplitComplexRing satisfies InvolutiveRingLaws" {
        InvolutiveRingLaws(
            ring = SplitComplexAlgebras.RealSplitComplexRing,
            arb = ArbSplitComplex.boundedSplitComplex,
            eq = SplitComplexAlgebras.eqSplitComplex,
            pr = SplitComplexAlgebras.printableSplitComplexPretty
        ).fullTest().throwIfFailed()
    }
})
