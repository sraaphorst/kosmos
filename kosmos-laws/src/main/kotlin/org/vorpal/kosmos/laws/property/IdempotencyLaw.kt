package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Idempotency Law. */
class IdempotencyLaw<A: Any>(
    private val op: BinOp<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {
    override val name = "idempotency ($symbol)"

    override suspend fun test() {
        checkAll(arb) { a ->
            val value = op(a, a)
            withClue(failMessage(a, value)) {
                check(eq.eqv(a, value))
            }
        }
    }

    private fun failMessage(
        a: A, value: A
    ): () -> String = {
        val sa = pr.render(a)
        val sValue = pr.render(value)

        buildString {
            appendLine("Idempotency failed:")
            append("$sa $symbol $sa")
            append(" = ")
            append(sValue)
            append(" (expected: $sa)")
            appendLine()
        }
    }
}
