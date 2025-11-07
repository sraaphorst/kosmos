package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
* Flexibility is a condition that is implied by but strictly weaker than alternativity.
* The classic example is the sedenions, which are flexible, but not alternative.
* The following condition must be met: for all x, y in the algebra:
*  * x(yx) = (xy)x
*/
class FlexibilityLaw<A: Any>(
    private val op: BinOp<A>,
    private val pairArb: Arb<Pair<A, A>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {

    /** Convenience constructor from a single-element generator. */
    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.pair(arb, arb), eq, pr, symbol)

    override val name: String = "flexibility ($symbol)"

    override suspend fun test() {
        checkAll(pairArb) { (x, y) ->
            // x (y x)
            val yx = op(y, x)
            val left = op(x, yx)

            // (x y) x
            val xy = op(x, y)
            val right = op(xy, x)

            withClue(failureMessage(x, y, yx, xy, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun failureMessage(
        x: A, y: A,
        yx: A, xy: A,
        left: A, right: A
    ): () -> String = {
        fun infix(l: String, r: String) = "$l $symbol $r"

        val sx  = pr.render(x)
        val sy  = pr.render(y)
        val syx = pr.render(yx)
        val sxy = pr.render(xy)
        val sLeft = pr.render(left)
        val sRight = pr.render(right)

        buildString {
            appendLine("Flexibility failed:")
            append(infix(sx, "(" + infix(sy, sx) + ")"))
            append(" = ")
            append(infix(sx, syx))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix("(" + infix(sx, sy) + ")", sx))
            append(" = ")
            append(infix(sxy, sx))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}