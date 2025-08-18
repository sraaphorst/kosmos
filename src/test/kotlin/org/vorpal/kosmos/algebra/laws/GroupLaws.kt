package org.vorpal.kosmos.algebra.laws

import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Eq
import io.kotest.property.Arb
import org.vorpal.kosmos.core.assertEquals

open class GroupLaws<A, S>(
    S: S,
    arb: Arb<A>,
    EQ: Eq<A>
) : MonoidLaws<A, S>(S, arb, EQ)
        where S : Group<A, *> {

    open suspend fun inverses() =
        io.kotest.property.checkAll(arb) { a ->
            EQ.assertEquals(S.combine(a, S.inverse(a)), S.identity)
            EQ.assertEquals(S.combine(S.inverse(a), a), S.identity)
        }

    override suspend fun all() {
        super.all()
        inverses()
    }
}