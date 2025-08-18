package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

open class AbelianGroupLaws<A, S>(
    S: S,
    arb: Arb<A>,
    EQ: Eq<A>
) : GroupLaws<A, S>(S, arb, EQ)
        where S : AbelianGroup<A, *> {

    suspend fun commutativity() =
        io.kotest.property.checkAll(arb, arb) { a, b ->
            EQ.assertEquals(S.combine(a, b), S.combine(b, a))
        }

    override suspend fun all() {
        super.all()
        commutativity()
    }
}
