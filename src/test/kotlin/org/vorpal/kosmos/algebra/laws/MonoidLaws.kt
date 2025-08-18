package org.vorpal.kosmos.algebra.laws

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import io.kotest.property.Arb
import org.vorpal.kosmos.core.assertEquals

open class MonoidLaws<A, S>(
    S: S,
    arb: Arb<A>,
    EQ: Eq<A>
) : SemigroupLaws<A, S>(S, arb, EQ)
        where S : Monoid<A, *> {

    open suspend fun identity() =
        io.kotest.property.checkAll(arb) { a ->
            EQ.assertEquals(S.combine(S.identity, a), a)
            EQ.assertEquals(S.combine(a, S.identity), a)
        }

    override suspend fun all() {
        super.all()
        identity()
    }
}
