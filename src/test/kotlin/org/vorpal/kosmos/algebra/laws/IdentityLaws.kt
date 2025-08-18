package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

class IdentityLaws<A, S>(
    private val S: S,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) where S : Monoid<A, *> {
    suspend fun leftRight() = checkAll(arb) { a ->
        EQ.assertEquals(S.combine(S.identity, a), a)
        EQ.assertEquals(S.combine(a, S.identity), a)
    }
}