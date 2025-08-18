package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Group
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

class InverseLaws<A, S>(
    private val S: S,
    private val arb: Arb<A>,
    private val EQ: Eq<A>,
    private val nonInvertible: Set<A> = emptySet()
) where S : Group<A, *> {

    // Check invertible elements for inverses.
    suspend fun existsAndCancels() {
        val invertible = arb.filter { a -> !nonInvertible.contains(a) }
        checkAll(invertible) { a ->
            EQ.assertEquals(S.combine(a, S.inverse(a)), S.identity)
            EQ.assertEquals(S.combine(S.inverse(a), a), S.identity)
        }
    }
}