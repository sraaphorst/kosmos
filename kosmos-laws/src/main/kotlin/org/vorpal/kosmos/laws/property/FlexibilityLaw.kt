package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Flexibility is a condition that is implied by but strictly weaker than alternativity:
 *
 * The classic example is the sedenions, which are flexible, but not alternative.
 *
 * The following condition must be met: for all `x, y` in the algebra:
 *
 *    x(yx) = (xy)x
 */
class FlexibilityLaw<A : Any>(
    private val op: BinOp<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {
    override val name: String = "flexibility (${op.symbol})"

    suspend fun flexibilityCheck() {
        checkAll(TestingLaw.arbPair(arb)) { (x, y) ->
            // x (y x)
            val yx = op(y, x)
            val left = op(x, yx)

            // (x y) x
            val xy = op(x, y)
            val right = op(xy, x)

            withClue(flexibilityFail(x, y, yx, xy, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun flexibilityFail(
        x: A, y: A,
        yx: A, xy: A,
        left: A, right: A
    ): () -> String = {
        fun expr(left: String, right: String) = "$left ${op.symbol} $right"

        buildString {
            val sx  = pr(x)
            val sy  = pr(y)
            val syx = pr(yx)
            val sxy = pr(xy)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Flexibility failed (${op.symbol}):")
            append(expr(sx, "(" + expr(sy, sx) + ")"))
            append(" = ")
            append(expr(sx, syx))
            append(" = ")
            appendLine(sLeft)

            append(expr("(" + expr(sx, sy) + ")", sx))
            append(" = ")
            append(expr(sxy, sx))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }

    override suspend fun test() =
        flexibilityCheck()
}
