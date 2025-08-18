package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

class FieldLaws<A>(
    private val F: Field<A>,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) {
    private val ring = RingLaws(F, arb, EQ)

    suspend fun ringAxioms() = ring.all()

    suspend fun multiplicativeAbelianGroup() {
        val G = F.mul
        val nonzero = arb.filter { a -> !EQ.eqv(a, F.add.identity) }  // a ≠ 0

        // commutativity
        checkAll(arb, arb) { a, b ->
            EQ.assertEquals(G.combine(a, b), G.combine(b, a))
        }

        // inverses exist on F\{0}
        checkAll(nonzero) { a ->
            val inv = G.inverse(a) // safe: generator never yields 0
            EQ.assertEquals(G.combine(a, inv), G.identity)
            EQ.assertEquals(G.combine(inv, a), G.identity)
        }
    }

    suspend fun all() {
        ringAxioms()
        multiplicativeAbelianGroup()
    }
}