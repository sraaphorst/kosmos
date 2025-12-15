package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Left division laws for a quasigroup:
 *
 *       a ⋆ (a \\ b) = b
 *       a \\ (a ⋆ b) = b
 */
class LeftDivisionLaw<A : Any>(
    private val op: BinOp<A>,
    private val leftDiv: BinOp<A>,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default()
) : TestingLaw {

    private val arbPair = TestingLaw.arbPair(arb)

    override val name: String =
        "left-division (${op.symbol}, ${leftDiv.symbol})"

    override suspend fun test() {
        checkAll(arbPair) { (a, b) ->
            val x = leftDiv(a, b)
            val ax = op(a, x)

            withClue(solvesEquationFailure(a, b, x, ax)) {
                check(eq(ax, b))
            }

            val ab = op(a, b)
            val back = leftDiv(a, ab)

            withClue(undoesLeftMulFailure(a, b, ab, back)) {
                check(eq(back, b))
            }
        }
    }

    private fun solvesEquationFailure(a: A, b: A, x: A, ax: A): () -> String = {
        fun mul(l: String, r: String) = "$l ${op.symbol} $r"
        fun ldiv(l: String, r: String) = "$l ${leftDiv.symbol} $r"

        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sx = pr(x)
            val sax = pr(ax)
            appendLine("Left division failed (solve):")
            appendLine("x = ${ldiv(sa, sb)} = $sx")
            appendLine("${mul(sa, sx)} = $sax")
            appendLine("Expected: $sax = $sb")
        }
    }

    private fun undoesLeftMulFailure(a: A, b: A, ab: A, back: A): () -> String = {
        fun mul(l: String, r: String) = "$l ${op.symbol} $r"
        fun ldiv(l: String, r: String) = "$l ${leftDiv.symbol} $r"

        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sab = pr(ab)
            val sback = pr(back)
            appendLine("Left division failed (undo):")
            appendLine("${mul(sa, sb)} = $sab")
            appendLine("${ldiv(sa, sab)} = $sback")
            appendLine("Expected: $sback = $sb")
        }
    }
}