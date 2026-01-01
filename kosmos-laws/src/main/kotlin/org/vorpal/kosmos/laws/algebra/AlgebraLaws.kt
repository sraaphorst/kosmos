package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Algebra] laws:
 * - [CommutativeRingLaws] (full)
 * - [RingLaws]
 * - [RModuleLaws]
 * - [algebraMulBilinearityLaws] (avoids distributivity retesting that would happen with BilinearityLaws)
 */
class AlgebraLaws<R : Any, A : Any>(
    algebra: Algebra<R, A>,
    scalarArb: Arb<R>,
    algebraArb: Arb<A>,
    eqR: Eq<R> = Eq.default(),
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
) : LawSuite {

    private val scalarDescription =
        "R[${algebra.scalars.add.op.symbol}${algebra.scalars.mul.op.symbol}]"

    private val algebraDescription =
        "A[${algebra.add.op.symbol}${algebra.mul.op.symbol}]"

    override val name = suiteName(
        "Algebra",
        scalarDescription,
        algebra.leftAction.symbol,
        algebraDescription
    )

    private val scalarRingLaws: CommutativeRingLaws<R> by lazy {
        CommutativeRingLaws(algebra.scalars, scalarArb, eqR, prR)
    }
    private val ringLaws = RingLaws(algebra, algebraArb, eqA, prA)
    private val moduleLaws = RModuleLaws(algebra, scalarArb, algebraArb, eqR, eqA, prR, prA)

    private val structureLaws: List<TestingLaw> =
        algebraMulBilinearityLaws(
            act = algebra.leftAction,
            mulA = algebra.mul.op,
            arbR = scalarArb,
            arbA = algebraArb,
            eqA = eqA,
            prR = prR,
            prA = prA
        )

    override fun laws(): List<TestingLaw> =
        ringLaws.laws() +
            moduleLaws.laws() +
            structureLaws

    override fun fullLaws(): List<TestingLaw> =
        scalarRingLaws.fullLaws() +
            ringLaws.fullLaws() +
            moduleLaws.laws() +
            structureLaws
}
