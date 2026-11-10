package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawMode
import org.vorpal.kosmos.laws.LawNode
import org.vorpal.kosmos.laws.StructuredLawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.asNode
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [StarAlgebra] laws:
 * - [NonAssociativeStarAlgebraLaws]
 * - [AssociativityLaw]
 */
class StarAlgebraLaws<R : Any, A : Any>(
    algebra: StarAlgebra<R, A>,
    scalarArb: Arb<R>,
    algebraArb: Arb<A>,
    eqR: Eq<R> = Eq.default(),
    eqA: Eq<A> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prA: Printable<A> = Printable.default(),
) : StructuredLawSuite {

    override val name = suiteName(
        "StarAlgebra",
        "R[${algebra.scalars.add.op.symbol}${algebra.scalars.mul.op.symbol}]",
        algebra.leftAction.symbol,
        "A[${algebra.add.op.symbol}${algebra.mul.op.symbol}]",
        algebra.conj.symbol
    )

    private val starAlgebraLaws: NonAssociativeStarAlgebraLaws<R, A> by lazy {
        NonAssociativeStarAlgebraLaws(algebra, scalarArb, algebraArb, eqR, eqA, prR, prA)
    }

    private val associativityLaw: TestingLaw by lazy {
        AssociativityLaw(algebra.mul.op, algebraArb, eqA, prA)
    }

    override fun children(mode: LawMode): List<LawNode> =
        listOf(starAlgebraLaws.node(mode), associativityLaw.asNode())

}
