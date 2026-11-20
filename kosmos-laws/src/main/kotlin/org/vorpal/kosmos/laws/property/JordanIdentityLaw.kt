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
 * The Jordan identity:
 *
 *    x ∘ (y ∘ x²) = (x ∘ y) ∘ x²
 *
 * This in combination with the [CommutativityLaw] gives a Jordan algebra,
 * which also implies that the [PowerAssociativityLaw] holds.
 */
class JordanIdentityLaw<A : Any>(
    private val op: BinOp<A>,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {
    private val arbPair = TestingLaw.arbPair(arb)
    override val name: String = "jordan identity (${op.symbol})"

    suspend fun jordanIdentityCheck() {
        lawCheckAll(arbPair, render = renderWith(pr)) { (x, y) ->
            val x2 = op(x, x)
            val left = op(x, op(y, x2))
            val right = op(op(x, y), x2)

            lawCheck(eq(left, right), jordanIdentityFail(x, y, x2, left, right))
        }
    }

    private fun jordanIdentityFail(
        x: A, y: A, x2: A, left: A, right: A
    ): () -> String = {
        fun expr(l: String, r: String) = "$l ${op.symbol} $r"

        buildString {
            val sx = pr(x)
            val sy = pr(y)
            val sx2 = pr(x2)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Jordan identity failed:")

            append(expr(sx, "(" + expr(sy, sx2) + ")"))
            append(" = ")
            appendLine(sLeft)

            append(expr("(" + expr(sx, sy) + ")", sx2))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }

    override suspend fun test() =
        jordanIdentityCheck()
}
