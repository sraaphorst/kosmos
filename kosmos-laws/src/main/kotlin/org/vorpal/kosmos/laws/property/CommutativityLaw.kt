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
 * Commutativity Law: `ab = ba`.
 */
class CommutativityLaw<A: Any>(
    private val op: BinOp<A>,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {
    private val arbPair = TestingLaw.arbPair(arb)
    override val name = "commutativity (${op.symbol})"

    override suspend fun test() {
        lawCheckAll(arbPair, render = renderWith(pr)) { (a, b) ->
            val left  = op(a, b)
            val right = op(b, a)

            lawCheck(eq(left, right), failureMessage(a, b, left, right))
        }
    }

    private fun failureMessage(
        a: A, b: A,
        left: A, right: A
    ): () -> String = {
        fun expr(left: String, right: String) = "$left ${op.symbol} $right"

        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Commutativity failed:")
            appendLine("${expr(sa, sb)} = $sLeft")
            appendLine("${expr(sb, sa)} = $sRight")
            appendLine("Expected: $sLeft = $sRight")
        }
    }
}
