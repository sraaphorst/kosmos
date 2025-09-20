package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.ops.BinOp
import org.vorpal.kosmos.algebra.props.Idempotent
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

class IdempotencyLaws<A, S>(
    private val S: S,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) where S: BinOp<A>, S : Idempotent {
    suspend fun holds() = checkAll(arb) { a ->
        EQ.assertEquals(S.combine(a, a), a)
    }
}