package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import kotlinx.coroutines.CancellationException
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawViolation
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

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
        lawCheckAll(arbPair, render = renderWith(pr)) { (a, b) ->
            try {
                op(a, b)
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                // The operation throwing is exactly what makes this law false.
                throw LawViolation(failureMessage(a, b, t)(), t)
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
