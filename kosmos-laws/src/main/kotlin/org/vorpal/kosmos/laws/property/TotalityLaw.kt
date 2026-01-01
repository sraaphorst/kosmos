package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import kotlinx.coroutines.CancellationException
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Totality / definedness law: `op(a, b)` should not throw for generated inputs.
 *
 * Not an algebraic axiom, but a very useful sanity check for "structure implementations"
 * that might accidentally be partial operations in disguise.
 */
class TotalityLaw<A : Any>(
    private val op: BinOp<A>,
    arb: Arb<A>,
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {

    private val arbPair = TestingLaw.arbPair(arb)

    override val name: String = "totality (${op.symbol})"

    override suspend fun test() {
        checkAll(arbPair) { (a, b) ->
            try {
                op(a, b)
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                withClue(failureMessage(a, b, t)) {
                    throw AssertionError("Totality failed.", t)
                }
            }
        }
    }

    private fun failureMessage(a: A, b: A, t: Throwable): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val ex = t::class.qualifiedName ?: t::class.simpleName ?: "Throwable"
            val msg = t.message

            appendLine("Totality failed: op threw an exception.")
            appendLine("op = ${op.symbol}")
            appendLine("a = $sa")
            appendLine("b = $sb")
            appendLine("exception = $ex")
            if (!msg.isNullOrBlank()) {
                appendLine("message = $msg")
            }
        }
    }
}
