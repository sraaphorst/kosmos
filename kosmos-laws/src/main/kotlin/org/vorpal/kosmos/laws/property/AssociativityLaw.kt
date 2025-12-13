package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Associativity Law
 *
 *    a(bc) == (ab)c
 */
class AssociativityLaw<A : Any>(
    private val op: BinOp<A>,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {
    private val tripleArb = TestingLaw.arbTriple(arb)
    override val name = "associativity (${op.symbol})"

    private fun expr(left: String, right: String) = "$left ${op.symbol} $right"

    private suspend fun associativityCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val bc = op(b, c)
            val left  = op(a, bc)

            val ab = op(a, b)
            val right = op(ab, c)

            withClue(failureMessage(a, b, c, ab, bc, left, right)) {
                check(eq(left, right))
            }
        }
    }

    /** Lazy evaluated failure message upon failed test case. */
    private fun failureMessage(
        a: A, b: A, c: A,
        ab: A, bc: A,
        left: A, right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sab = pr(ab)
            val sbc = pr(bc)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Associativity failed:")

            append(expr(sa, "(" + expr(sb, sc) + ")"))
            append(" = ")
            append(expr(sa, sbc))
            append(" = ")
            appendLine(sLeft)

            append(expr("(" + expr(sa, sb) + ")", sc))
            append(" = ")
            append(expr(sab, sc))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }

    override suspend fun test() =
        associativityCheck()
}
