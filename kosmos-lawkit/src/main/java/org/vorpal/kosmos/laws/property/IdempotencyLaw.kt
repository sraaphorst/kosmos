package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.TestingLaw
import org.vorpal.kosmos.core.ops.BinOp

/** Idempotency Law. */
class IdempotencyLaw<A>(
    private val op: BinOp<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>
) : TestingLaw {
    override val name = "idempotency"

    override suspend fun test() {
        checkAll(arb) { a ->
            val left  = op.combine(a, a)
            withClue("Idempotency failed: $a * $a = $left") {
                check(eq.eqv(left, a))
            }
        }
    }
}
