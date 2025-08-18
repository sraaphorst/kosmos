package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

class AssociativityLaws<A, S>(
    private val S: S,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) where S : Semigroup<A> {
    suspend fun holds() = checkAll(arb, arb, arb) { a, b, c ->
        EQ.assertEquals(S.combine(a, S.combine(b, c)), S.combine(S.combine(a, b), c))
    }
}