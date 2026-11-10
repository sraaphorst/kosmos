package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith


/**
 * Idempotency Law for a binary operation:
 *
 * For all `a ∈ A`:
 *
 *     a ⋆ a = a
 *
 */
class IdempotencyLaw<A: Any>(
    private val op: BinOp<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {
    override val name = "idempotency (${op.symbol})"

    suspend fun idempotencyCheck() {
        lawCheckAll(arb, render = renderWith(pr)) { a ->
            val value = op(a, a)
            lawCheck(eq(a, value), idempotencyFail(a, value))
        }
    }

    private fun idempotencyFail(
        a: A, value: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sValue = pr(value)

            appendLine("Idempotency failed:")
            append("$sa ${op.symbol} $sa = $sValue")
            appendLine("Expected: $sa")
        }
    }

    override suspend fun test() =
        idempotencyCheck()
}
