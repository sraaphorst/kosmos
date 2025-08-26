package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.ops.BinOp
import org.vorpal.kosmos.algebra.props.Commutative
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

class CommutativityLaws<A, S>(
    private val S: S,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) where S : BinOp<A>, S : Commutative {
    suspend fun holds() = checkAll(arb, arb) { a, b ->
        EQ.assertEquals(S.combine(a, b), S.combine(b, a))
    }
}