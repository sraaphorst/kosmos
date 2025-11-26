package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.Eq


/**
 * An Abelian group follows group laws and must be commutative.
 */
open class AbelianGroupLaws<A: Any, S>(
    candidate: S,
    arb: Arb<A>,
    eq: Eq<A>
) : GroupLaws<A, S>(candidate, arb, eq)
        where S : AbelianGroup<A> {

    private val commutativity = CommutativityLaws(candidate, arb, eq)

    override suspend fun all() {
        super.all()
        commutativity.holds()
    }
}