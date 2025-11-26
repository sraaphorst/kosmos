package org.vorpal.kosmos.algebra.laws

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import io.kotest.property.Arb
import org.vorpal.kosmos.org.vorpal.kosmos.laws.SemigroupLaws
import org.vorpal.kosmos.laws.property.IdentityLaws

open class MonoidLaws<A, S>(
    S: S,
    arb: Arb<A>,
    EQ: Eq<A>
) : SemigroupLaws<A, S>(S, arb, EQ)
        where S : Monoid<A, *> {

    private val identity = IdentityLaws(S, arb, EQ)

    override suspend fun all() {
        super.all()
        identity.leftRight()
    }
}
