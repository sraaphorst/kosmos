package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Right division laws for a quasigroup:
 *
 *     (b / a) ⋆ a = b
 *     (b ⋆ a) / a = b
 *
 * Note: This assumes the API contract that `rightDiv(a, b)` returns `y` such that `y ⋆ a = b`.
 * If your parameter order is (b, a) instead, swap the calls accordingly.
 *
 * TODO: Cleanup comments.
 */
class RightDivisionLaw<A : Any>(
    private val op: BinOp<A>,
    private val rightDiv: BinOp<A>,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val arbPair = TestingLaw.arbPair(arb)

    override val name: String =
        "right-division (${op.symbol}, ${rightDiv.symbol})"

    override suspend fun test() {
        checkAll(arbPair) { (a, b) ->
            // y = (a ? b) such that y ⋆ a = b  (per your stated contract)
            val y = rightDiv(a, b)
            val ya = op(y, a)

            withClue(solvesEquationFailure(a, b, y, ya)) {
                check(eq(ya, b))
            }

            val ba = op(b, a)
            val back = rightDiv(a, ba)

            withClue(undoesRightMulFailure(a, b, ba, back)) {
                check(eq(back, b))
            }
        }
    }

    private fun solvesEquationFailure(a: A, b: A, y: A, ya: A): () -> String = {
        fun mul(l: String, r: String) = "$l ${op.symbol} $r"
        fun rdiv(l: String, r: String) = "$l ${rightDiv.symbol} $r"

        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sy = pr(y)
            val sya = pr(ya)
            appendLine("Right division failed (solve):")
            appendLine("y = ${rdiv(sa, sb)} = $sy")
            appendLine("${mul(sy, sa)} = $sya")
            appendLine("Expected: $sya = $sb")
        }
    }

    private fun undoesRightMulFailure(a: A, b: A, ba: A, back: A): () -> String = {
        fun mul(l: String, r: String) = "$l ${op.symbol} $r"
        fun rdiv(l: String, r: String) = "$l ${rightDiv.symbol} $r"

        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sba = pr(ba)
            val sback = pr(back)
            appendLine("Right division failed (undo):")
            appendLine("${mul(sb, sa)} = $sba")
            appendLine("${rdiv(sa, sba)} = $sback")
            appendLine("Expected: $sback = $sb")
        }
    }
}