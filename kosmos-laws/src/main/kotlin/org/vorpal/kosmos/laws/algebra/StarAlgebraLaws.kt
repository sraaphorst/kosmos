package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [StarAlgebra] laws:
 * - [AlgebraLaws]
 * - [ConjugationLaws]
 * - star commutes with scalar action: (r ⊳ a)* = r ⊳ (a*)
 */
class StarAlgebraLaws<R : Any, A : Any>(
    algebra: StarAlgebra<R, A>,
    scalarArb: Arb<R>,
    algebraArb: Arb<A>,
    eqR: Eq<R> = Eq.default(),
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
) : LawSuite {

    override val name = suiteName(
        "StarAlgebra",
        "R[${algebra.scalars.add.op.symbol}${algebra.scalars.mul.op.symbol}]",
        algebra.leftAction.symbol,
        "A[${algebra.add.op.symbol}${algebra.mul.op.symbol}]",
        algebra.conj.symbol
    )

    private val algebraLaws: AlgebraLaws<R, A> by lazy {
        AlgebraLaws(algebra, scalarArb, algebraArb, eqR, eqA, prR, prA)
    }

    private val conjugationLaws: ConjugationLaws<A> by lazy {
        ConjugationLaws(
            conj = algebra.conj,
            add = algebra.add,
            mul = algebra.mul,
            arb = algebraArb,
            eq = eqA,
            pr = prA
        )
    }

    private val structureLaws: List<TestingLaw> = listOf(
        starCommutesWithScalarActionLaw(
            act = algebra.leftAction,
            star = algebra.conj,
            arbR = scalarArb,
            arbA = algebraArb,
            eqA = eqA,
            prR = prR,
            prA = prA
        )
    )

    override fun laws(): List<TestingLaw> =
        algebraLaws.laws() +
            conjugationLaws.laws() +
            structureLaws

    override fun fullLaws(): List<TestingLaw> =
        algebraLaws.fullLaws() +
            conjugationLaws.laws() +
            structureLaws
}