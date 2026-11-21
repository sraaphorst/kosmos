package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.AlgebraLaws
import org.vorpal.kosmos.laws.algebra.CommutativeRingLaws
import org.vorpal.kosmos.laws.algebra.RingLaws
import org.vorpal.kosmos.laws.algebra.SemiringLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instance.arbDenseMat
import org.vorpal.kosmos.linear.instance.arbSquareDenseMat
import org.vorpal.kosmos.testing.laws.lawTests

/**
 * Algebraic-law coverage for the matrix structures in [DenseMatAlgebras], over the rationals.
 *
 * Matrix multiplication is associative but not commutative, so the standard matrix structures
 * are exercised with [RingLaws]/[SemiringLaws]/[AlgebraLaws] (not their commutative variants),
 * while the entrywise (Hadamard) product gives a genuine commutative ring.
 */
class DenseMatStructuresSpec : FunSpec({
    lawTests(lawGraph("DenseMat structures") {
        val q = RationalAlgebras.RationalField
        val eqQ = RationalAlgebras.eqRational
        val prQ = RationalAlgebras.printableRationalPretty
        val eqMat = DenseMatAlgebras.liftEq(eqQ)
        val prMat = DenseMatAlgebras.liftPrintablePretty(prQ)

        val n = 4

        branch("DenseMatRing satisfies RingLaws over the rationals") {
            suite(RingLaws(
                ring = DenseMatAlgebras.DenseMatRing(q, n),
                arb = arbSquareDenseMat(ArbRational.small, n),
                eq = eqMat,
                pr = prMat
            ))
        }

        branch("DenseMatSemiring satisfies SemiringLaws over the rationals") {
            suite(SemiringLaws(
                semiring = DenseMatAlgebras.DenseMatSemiring(q, n),
                arb = arbSquareDenseMat(ArbRational.small, n),
                eq = eqMat,
                pr = prMat
            ))
        }

        branch("DenseMatAlgebra satisfies AlgebraLaws over the rationals") {
            suite(AlgebraLaws(
                algebra = DenseMatAlgebras.DenseMatAlgebra(q, n),
                scalarArb = ArbRational.small,
                algebraArb = arbSquareDenseMat(ArbRational.small, n),
                eqR = eqQ,
                eqA = eqMat,
                prR = prQ,
                prA = prMat
            ))
        }

        branch("DenseMatHadamardCommutativeRing satisfies CommutativeRingLaws over the rationals") {
            suite(CommutativeRingLaws(
                ring = DenseMatAlgebras.DenseMatHadamardCommutativeRing(q, n, n),
                arb = arbDenseMat(ArbRational.small, n, n),
                eq = eqMat,
                pr = prMat
            ))
        }

        branch("DenseMatHadamardUnitGroup satisfies AbelianGroupLaws over the rationals") {
            suite(AbelianGroupLaws(
                group = DenseMatAlgebras.DenseMatHadamardUnitGroup(q, n, n),
                arb = arbDenseMat(ArbRational.nonZero, n, n),
                eq = eqMat,
                pr = prMat
            ))
        }
    })
})
