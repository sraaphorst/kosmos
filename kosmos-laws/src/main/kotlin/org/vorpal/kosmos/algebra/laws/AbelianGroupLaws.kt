package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.laws.property.CommutativityLaws

/**
 * An Abelian group follows group laws and must be commutative.
 */
open class AbelianGroupLaws<A, S>(
    S: S,
    arb: Arb<A>,
    EQ: Eq<A>
) : GroupLaws<A, S>(S, arb, EQ)
        where S : AbelianGroup<A, *> {

    private val commutativity = CommutativityLaws(S, arb, EQ)

    override suspend fun all() {
        super.all()
        commutativity.holds()
    }
}