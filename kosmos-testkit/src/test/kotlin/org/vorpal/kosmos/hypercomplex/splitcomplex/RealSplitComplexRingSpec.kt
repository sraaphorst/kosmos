package org.vorpal.kosmos.hypercomplex.splitcomplex

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.hypercomplex.splitcomplex.SplitComplexAlgebras.diagPlusMinus
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.InvolutiveRingLaws
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Property tests for the split-complex ring over the Reals,
 * `RealSplitComplexRing`, which is `CommutativeInvolutiveRing<SplitComplex<Real>>`.
 *
 * Mirrors `ComplexFieldSpec`, except that `SplitComplex<Real>` is *not* a field
 * (it has zero divisors on the null cone `a² = b²`), so the strongest abstract
 * laws we can test here are those of a commutative involutive ring.
 */
class RealSplitComplexRingSpec : FunSpec({
    lawTests(lawGraph("split-complex ring over ℝ") {
        branch("RealSplitComplexRing satisfies CommutativeRingLaws") {
            suite(CommutativeRingLaws(
                ring = SplitComplexAlgebras.RealSplitComplexRing,
                arb = ArbSplitComplex.boundedSplitComplex,
                eq = SplitComplexAlgebras.eqSplitComplex,
                pr = SplitComplexAlgebras.printableSplitComplexPretty
            ))
        }

        branch("RealSplitComplexRing satisfies InvolutiveRingLaws") {
            suite(InvolutiveRingLaws(
                ring = SplitComplexAlgebras.RealSplitComplexRing,
                arb = ArbSplitComplex.boundedSplitComplex,
                eq = SplitComplexAlgebras.eqSplitComplex,
                pr = SplitComplexAlgebras.printableSplitComplexPretty
            ))
        }

        local("Diagonal basis converts in both directions") {
            val eq = SplitComplexAlgebras.eqSplitComplex
            lawCheckAll(ArbSplitComplex.splitComplex) { splitComplex ->
                val (diagPlus, diagMinus) = splitComplex.diagPlusMinus()
                val roundTrip = SplitComplexAlgebras.fromDiagonal(diagPlus, diagMinus)
                lawCheck(eq(roundTrip, splitComplex))
            }
        }
    })
})
