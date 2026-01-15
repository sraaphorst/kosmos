package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.module.leftRModuleLaws
import org.vorpal.kosmos.laws.suiteName

/**
 * [VectorSpace] laws (for a vector space `V` over a field `F`):
 * - [FieldLaws] (full)
 * - [AbelianGroupLaws] (full)
 *
 * Core structure laws are exactly the module axioms.
 */
class VectorSpaceLaws<F : Any, V : Any>(
    space: VectorSpace<F, V>,
    scalarArb: Arb<F>,
    vectorArb: Arb<V>,
    eqF: Eq<F> = Eq.default(),
    eqV: Eq<V> = Eq.default(),
    prF: Printable<F> = Printable.default(),
    prV: Printable<V> = Printable.default()
) : LawSuite {

    private val fieldDescription =
        "F[${space.scalars.add.op.symbol}${space.scalars.mul.op.symbol}]"

    private val vectorDescription =
        "V[${space.add.op.symbol}]"

    override val name = suiteName(
        "VectorSpace",
        fieldDescription,
        space.leftAction.symbol,
        vectorDescription
    )

    private val fieldLaws: FieldLaws<F> by lazy {
        FieldLaws(space.scalars, scalarArb, eqF, prF)
    }
    private val groupLaws: AbelianGroupLaws<V> by lazy {
        AbelianGroupLaws(space.add, vectorArb, eqV, prV)
    }

    private val structureLaws: List<TestingLaw> =
        leftRModuleLaws(space, scalarArb, vectorArb, eqV, prF, prV)

    override fun laws(): List<TestingLaw> =
        structureLaws

    override fun fullLaws(): List<TestingLaw> =
        fieldLaws.fullLaws() +
            groupLaws.fullLaws() +
            structureLaws
}
