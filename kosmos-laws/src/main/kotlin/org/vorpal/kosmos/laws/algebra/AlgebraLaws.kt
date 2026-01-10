package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.AssociativityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [Algebra] laws:
 * - [NonAssociativeAlgebraLaws]
 * - [AssociativityLaw]
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

    private val nonAssociativeAlgebraLaws = NonAssociativeAlgebraLaws(
        algebra, scalarArb, algebraArb, eqR, eqA, prR, prA
    )

    private val structureLaws: List<TestingLaw> = listOf(
        AssociativityLaw(
            algebra.mul.op, algebraArb, eqA, prA
        )
    )

    override fun laws(): List<TestingLaw> =
        nonAssociativeAlgebraLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        nonAssociativeAlgebraLaws.fullLaws() + structureLaws
}
