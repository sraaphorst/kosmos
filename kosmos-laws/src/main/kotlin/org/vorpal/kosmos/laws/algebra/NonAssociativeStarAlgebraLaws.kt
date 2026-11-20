package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawBinding
import org.vorpal.kosmos.laws.LawComponent
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.asNodes
import org.vorpal.kosmos.laws.suiteName

/**
 * [NonAssociativeStarAlgebra] laws
 * - [NonAssociativeAlgebraLaws]
 * - [ConjugationLaws]
 * - [starCommutesWithScalarActionLaw]
 */
class NonAssociativeStarAlgebraLaws<R : Any, A : Any>(
    algebra: NonAssociativeStarAlgebra<R, A>,
    scalarArb: Arb<R>,
    algebraArb: Arb<A>,
    eqR: Eq<R> = Eq.default(),
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default()
) : LawSuite {

    override val name = suiteName(
        "NonAssociativeStarAlgebra",
        "R[${algebra.scalars.add.op.symbol}${algebra.scalars.mul.op.symbol}]",
        algebra.leftAction.symbol,
        "A[${algebra.add.op.symbol}${algebra.mul.op.symbol}]",
        algebra.conj.symbol
    )

    override val binding = LawBinding.of(this, algebra, scalarArb, algebraArb, eqR, eqA, prR, prA)

    private val algebraLaws: NonAssociativeAlgebraLaws<R, A> by lazy {
        NonAssociativeAlgebraLaws(algebra, scalarArb, algebraArb, eqR, eqA, prR, prA)
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

    private val structureLaws: List<LawComponent> = listOf(
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

    override fun children(): List<LawNode> =
        listOf(algebraLaws.node(), conjugationLaws.node()) +
            structureLaws.asNodes()

}
