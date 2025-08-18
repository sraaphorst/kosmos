package org.vorpal.kosmos.algebra.laws

import org.vorpal.kosmos.algebra.structures.Semigroup
import org.vorpal.kosmos.core.Eq
import io.kotest.property.Arb
import org.vorpal.kosmos.core.assertEquals

open class SemigroupLaws<A, S>(
    protected val S: S,
    protected val arb: Arb<A>,
    protected val EQ: Eq<A>
) where S : Semigroup<A> {

    open suspend fun associativity() =
        io.kotest.property.checkAll(arb, arb, arb) { a, b, c ->
            EQ.assertEquals(S.combine(a, S.combine(b, c)), S.combine(S.combine(a, b), c))
        }

    open suspend fun all() { associativity() }
}
