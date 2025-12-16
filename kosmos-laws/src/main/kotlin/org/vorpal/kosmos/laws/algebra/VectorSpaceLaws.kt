package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.suiteName

/**
 * [VectorSpace] laws:
 * - [RModuleLaws]
 */
class VectorSpaceLaws<F : Any, V : Any>(
    private val space: VectorSpace<F, V>,
    private val scalarArb: Arb<F>,
    private val vectorArb: Arb<V>,
    private val eqV: Eq<V> = Eq.default(),
    private val prF: Printable<F> = Printable.default(),
    private val prV: Printable<V> = Printable.default()
) : LawSuite by RModuleLaws(space, scalarArb, vectorArb, eqV, prF, prV) {
    private val leftScalarDescription = "F[${space.scalars.add.op.symbol}${space.scalars.mul.op.symbol}]"
    private val vectorSpaceDescription = "V[${space.group.op.symbol}]"
    override val name = suiteName(
        "VectorSpace",
        leftScalarDescription,
        space.leftAction.symbol,
        vectorSpaceDescription,
    )
}