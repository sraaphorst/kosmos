package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.instance.arbDenseVec
import org.vorpal.kosmos.testing.laws.lawTests

/**
 * Algebraic-law coverage for the vector structures in [DenseVecAlgebras], over the rationals.
 */
class DenseVecStructuresSpec : FunSpec({
    lawTests(lawGraph("DenseVec structures") {
        val q = RationalAlgebras.RationalField
        val eqQ = RationalAlgebras.eqRational
        val prQ = RationalAlgebras.printableRationalPretty
        val eqVec = DenseVecAlgebras.liftEq(eqQ)
        val prVec = DenseVecAlgebras.liftPrintablePretty(prQ)

        val n = 3

        branch("DenseVecGroup satisfies AbelianGroupLaws over the rationals") {
            suite(AbelianGroupLaws(
                group = DenseVecAlgebras.DenseVecGroup(q.add, n),
                arb = arbDenseVec(ArbRational.small, n),
                eq = eqVec,
                pr = prVec
            ))
        }

        branch("DenseVectorSpace satisfies VectorSpaceLaws over the rationals") {
            suite(VectorSpaceLaws(
                space = DenseVecAlgebras.DenseVectorSpace(q, n),
                scalarArb = ArbRational.small,
                vectorArb = arbDenseVec(ArbRational.small, n),
                eqF = eqQ,
                eqV = eqVec,
                prF = prQ,
                prV = prVec
            ))
        }

        branch("DenseVecHadamardUnitGroup satisfies AbelianGroupLaws over the rationals") {
            suite(AbelianGroupLaws(
                group = DenseVecAlgebras.DenseVecHadamardUnitGroup(q, n),
                arb = arbDenseVec(ArbRational.nonZero, n),
                eq = eqVec,
                pr = prVec
            ))
        }
    })
})
