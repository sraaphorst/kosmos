package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** Checks the Jordan identity:
 * * x ∘ (y ∘ x²) = (x ∘ y) ∘ x².
 *
 * This in combination with the CommutativityLaw gives a Jordan algebra,
 * which also implies that the PowerAssociativityLaw holds. */
class JordanIdentityLaw<A>(
    private val op: BinOp<A>,
    private val pairArb: Arb<Pair<A, A>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "∘"
) : TestingLaw {

    constructor(
        op: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "∘"
    ) : this(op, Arb.pair(arb, arb), eq, pr, symbol)

    override val name: String = "Jordan identity ($symbol)"

    override suspend fun test() {
        checkAll(pairArb) { (x, y) ->
            val x2 = op.combine(x, x)
            val left = op.combine(x, op.combine(y, x2))
            val right = op.combine(op.combine(x, y), x2)

            withClue(failureMessage(x, y, x2, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun failureMessage(
        x: A, y: A, x2: A, left: A, right: A
    ): () -> String = {
        fun infix(l: String, r: String) = "$l $symbol $r}"

        val sx = pr.render(x)
        val sy = pr.render(y)
        val sx2 = pr.render(x2)
        val sLeft = pr.render(left)
        val sRight = pr.render(right)

        buildString {
            appendLine("Jordan identity failed:")

            append(infix(sx, "(" + infix(sy, sx2) + ")"))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix("(" + infix(sx, sy) + ")", sx2))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}
